package exifdistance;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import bitwise.apps.focusscan.scan.DistanceFile;
import bitwise.apps.focusscan.scan.DistanceFileDatum;
import bitwise.apps.focusscan.scan.ScanFile;
import bitwise.apps.focusscan.scan.ScanFileDatum;

public class ExifDistance {

	public static void main(String[] args) {
		if (args.length < 1) {
			System.out.println("Usage: ExifDistance scan-root-directory OR");
			System.out.println("       ExifDistance distance-file.xml");
		}
		
		Path scanPath = Paths.get(args[0]);
		Path scanManifest = null;
		
		if (Files.isDirectory(scanPath)) {
			scanManifest = scanPath.resolve("scanManifest.xml");
			if (!Files.exists(scanManifest)) {
				System.out.format("Error: scan file `%s` does not exist.\n", scanManifest);
				return;		
			}
			
			if (!Files.isReadable(scanManifest)) {
				System.out.format("Error: scan file `%s` is not readable.\n", scanManifest);
			}
			readScanManifest(scanManifest, scanPath);
		} else {
			readDistanceManifest(scanPath);
		}
		

	}
	
	public static void readScanManifest(Path scanManifest, Path scanPath) {
		try {
			ScanFile scanFile = new ScanFile(scanManifest);
			System.out.format("Images in scan: %d\n", scanFile.getData().size());
			
			DistanceFile distanceFile = new DistanceFile(); 
			distanceFile.setScanPath(scanPath.toString());

			for (ScanFileDatum scanFileDatum : scanFile.getData()) {
				Path siPath = scanPath.resolve(scanFileDatum.getStillImage());
				ArrayList<Float> dist = ExifReader.readDistance(siPath);
				DistanceFileDatum distFileDatum = new DistanceFileDatum();
				distFileDatum.setPath(siPath.toString());
				distFileDatum.setImageNumber(scanFileDatum.getImageNumber());
				distFileDatum.setDistanceInMeters(dist);
				distanceFile.getData().add(distFileDatum);
			}
			
			distanceFile.saveToFile(Paths.get("distanceFile.xml"));
		} catch (ParserConfigurationException | SAXException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void readDistanceManifest(Path distanceManifest) {
		try {
			DistanceFile distFile = new DistanceFile(distanceManifest);
			DistanceFile outputFile = new DistanceFile();
			
			outputFile.setScanPath(distFile.getScanPath());
			Float begin = -1.0f;
			Float curr = -1.0f;
			int steps = 0;
			int baseImageNumber = 0;
			for (DistanceFileDatum distFileDatum : distFile.getData()) {

				curr = distFileDatum.getDistanceInMeters().get(0);

				if (begin < 0) {
					begin = curr;
				} else if (Math.abs(begin.floatValue() - curr.floatValue()) > 0.001) {

					for (int i = 0; i < steps; ++i) {
						DistanceFileDatum outDatum = new DistanceFileDatum();
						DistanceFileDatum origDatum = distFile.getDataByImageNumber(baseImageNumber + i + 1);
						outDatum.setPath(origDatum.getPath());
						outDatum.setImageNumber(origDatum.getImageNumber());
						outDatum.setDistanceInMeters(new ArrayList<Float>(Arrays.asList(begin + (curr - begin) / steps * i)));
						outputFile.getData().add(outDatum);
					}
					
					baseImageNumber = distFileDatum.getImageNumber();
					begin = curr;
					steps = 0;
				} else steps++;
			}
			for (int i = 0; i < steps; ++i) {
				DistanceFileDatum outDatum = new DistanceFileDatum();
				DistanceFileDatum origDatum = distFile.getDataByImageNumber(baseImageNumber + i + 1);
				outDatum.setPath(origDatum.getPath());
				outDatum.setImageNumber(origDatum.getImageNumber());
				outDatum.setDistanceInMeters(new ArrayList<Float>(Arrays.asList((curr - begin) / steps * i)));
				outputFile.getData().add(outDatum);
			}
			outputFile.saveToFile(Paths.get("distManifest2.xml"));
		} catch (ParserConfigurationException | SAXException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
