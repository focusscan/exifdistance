package exifdistance;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import bitwise.apps.focusscan.scan.DistanceFile;
import bitwise.apps.focusscan.scan.DistanceFileDatum;
import bitwise.apps.focusscan.scan.ScanFile;
import bitwise.apps.focusscan.scan.ScanFileDatum;

public class ExifDistance {

	public static void main(String[] args) {
		if (args.length < 2) {
			System.out.println("Usage: ExifDistance scan-root-directory output-file");
			return;
		}
		
		Path scanPath = Paths.get(args[0]);
		Path scanManifest = null;
		
		scanManifest = scanPath.resolve("scanManifest.xml");
		if (!Files.exists(scanManifest)) {
			System.out.format("Error: scan file `%s` does not exist.\n", scanManifest);
			return;		
		}
		
		if (!Files.isReadable(scanManifest)) {
			System.out.format("Error: scan file `%s` is not readable.\n", scanManifest);
		}
		readScanManifest(scanManifest, scanPath, Paths.get(args[1]));
	}
	
	public static void readScanManifest(Path scanManifest, Path scanPath, Path output) {
		try {
			ScanFile scanFile = new ScanFile(scanManifest);
			System.out.format("Images in scan: %d\n", scanFile.getData().size());
			
			DistanceFile distanceFile = new DistanceFile(); 
			distanceFile.setScanPath(scanPath.toString());
			distanceFile.set35MMFocalLength(ExifReader.read35MMFocalLength(scanPath.resolve(scanFile.getData().get(0).getStillImage())));

			for (ScanFileDatum scanFileDatum : scanFile.getData()) {
				Path siPath = scanPath.resolve(scanFileDatum.getStillImage());
				ArrayList<Float> dist = ExifReader.readDistance(siPath);
				DistanceFileDatum distFileDatum = new DistanceFileDatum();
				distFileDatum.setPath(siPath.toString());
				distFileDatum.setImageNumber(scanFileDatum.getImageNumber());
				distFileDatum.setDistanceInMeters(dist);
				distanceFile.getData().add(distFileDatum);
			}
			
			distanceFile.saveToFile(output);
		} catch (ParserConfigurationException | SAXException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
