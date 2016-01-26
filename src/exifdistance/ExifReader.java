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
import com.drew.metadata.exif.makernotes.CanonMakernoteDirectory;
import com.drew.metadata.exif.makernotes.NikonType2MakernoteDescriptor;
import com.drew.metadata.exif.makernotes.NikonType2MakernoteDirectory;

public class ExifReader {

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
            	return new ArrayList<Float>(Arrays.asList(Float.valueOf(fd.substring(0, fd.length() - 2))));
            }
          
        } catch (ImageProcessingException | IOException | MetadataException e) {
            System.out.println("Exception " + e);
        } 
        
        return null;
	}
}
