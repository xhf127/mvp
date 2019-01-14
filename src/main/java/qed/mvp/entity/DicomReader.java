package qed.mvp.entity;

import lombok.Data;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.UID;
import org.dcm4che3.io.DicomInputStream;
import org.dcm4che3.io.DicomInputStream.IncludeBulkData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

@Data
public class DicomReader {

    private static final Logger LOG = LoggerFactory.getLogger(DicomReader.class);

    private Attributes dataSet;
    private Attributes fmi;

    public DicomReader(File file) {
        DicomInputStream dis = null;
        try {
            dis = new DicomInputStream(file);
            dis.setIncludeBulkData(IncludeBulkData.URI);
            this.dataSet = dis.readDataset(-1, -1);
            this.fmi = dis.readFileMetaInformation() != null ? dis.readFileMetaInformation() : dataSet.createFileMetaInformation(UID.ImplicitVRLittleEndian);

        } catch (IOException e) {
            LOG.error(e.getMessage());

        } finally {
            try {
                if (dis != null) dis.close();
            } catch (IOException e) {
                LOG.error(e.getMessage());
            }
        }
    }

    public DicomReader(InputStream inputStream) {
        DicomInputStream dis = null;
        try {
            dis = new DicomInputStream(inputStream);
            dis.setIncludeBulkData(IncludeBulkData.URI);
            this.dataSet = dis.readDataset(-1, -1);
            this.fmi = dis.readFileMetaInformation() != null ? dis.readFileMetaInformation() : dataSet.createFileMetaInformation(UID.ImplicitVRLittleEndian);


        } catch (IOException e) {
            LOG.error(e.getMessage());

        } finally {
            try {
                if (dis != null) dis.close();
            } catch (IOException e) {
                LOG.error(e.getMessage());
            }
        }
    }

}
