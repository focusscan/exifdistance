package exifdistance;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;
import com.drew.metadata.MetadataException;
import com.drew.metadata.exif.ExifDirectoryBase;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.drew.metadata.exif.makernotes.CanonMakernoteDirectory;
import com.drew.metadata.exif.makernotes.NikonType2MakernoteDescriptor;
import com.drew.metadata.exif.makernotes.NikonType2MakernoteDirectory;

public class ExifReader {
	
	private static final float CanonCropFactor = 1.6f;

	public static ArrayList<Float> readDistance(Path filename) {
		
		File img = new File(filename.toString());
		
        try {
            Metadata metadata = ImageMetadataReader.readMetadata(img);
            CanonMakernoteDirectory dir = metadata.getFirstDirectoryOfType(CanonMakernoteDirectory.class);
            
            if (dir != null) {
	            Object file_info = dir.getObject(CanonMakernoteDirectory.TAG_FILE_INFO_ARRAY);
	            if (file_info.getClass().isArray())
	            	return new ArrayList<Float>(Arrays.asList(Array.getInt(file_info, 21) / 100.0f, 
	            							    			  Array.getInt(file_info, 20) / 100.f));
            }
            
            NikonType2MakernoteDirectory dir2 = metadata.getFirstDirectoryOfType(NikonType2MakernoteDirectory.class);
            
            if (dir2 != null) {     
                NikonType2MakernoteDescriptor desc = new NikonType2MakernoteDescriptor(dir2);
                String fd = desc.getLensFocusDistance();
            	return new ArrayList<Float>(Arrays.asList(Float.valueOf(fd.substring(0, fd.length() - 1))));
            }
          
        } catch (ImageProcessingException | IOException | MetadataException e) {
            System.out.println("Exception " + e);
        } 
        
        return null;
	}
	
	public static float read35MMFocalLength(Path filename) {
		File img = new File(filename.toString());
		
        try {
            Metadata metadata = ImageMetadataReader.readMetadata(img);
            ExifSubIFDDirectory dir = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
            
			Float fl = dir.getFloat(ExifDirectoryBase.TAG_FOCAL_LENGTH);
			
			ExifIFD0Directory dir2 = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);
			
			String model = dir2.getString(ExifDirectoryBase.TAG_MODEL);
			if (model.equalsIgnoreCase("Canon EOS 7D")) {
				fl *= CanonCropFactor;
			}
			
			return fl;
        } catch (ImageProcessingException | IOException | MetadataException e) {
            System.out.println("Exception " + e);
        } 
        
        return 0;	
	}
}
