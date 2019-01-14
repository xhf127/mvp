package qed.mvp.entity;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

public class OutBeanInfo extends SimpleBeanInfo {

    @Override
    public PropertyDescriptor[] getPropertyDescriptors() {
        PropertyDescriptor[] props = new PropertyDescriptor[28];
        try {
            props[0] = new PropertyDescriptor("err", Out.class);
            props[1] = new PropertyDescriptor("dim", Out.class);
            props[2] = new PropertyDescriptor("spacing", Out.class);
            props[3] = new PropertyDescriptor("origin", Out.class);
            props[4] = new PropertyDescriptor("clipfile", Out.class);
            props[5] = new PropertyDescriptor("file", Out.class);
            props[6] = new PropertyDescriptor("p", Out.class);
            props[7] = new PropertyDescriptor("k", Out.class);
            props[8] = new PropertyDescriptor("unit", Out.class);
            props[9] = new PropertyDescriptor("outdir", Out.class);
            props[10] = new PropertyDescriptor("clipdir", Out.class);
            props[11] = new PropertyDescriptor("bqcount", Out.class);
            props[12] = new PropertyDescriptor("bqlist", Out.class);
            props[13] = new PropertyDescriptor("suvcount", Out.class);
            props[14] = new PropertyDescriptor("suvlist", Out.class);
            props[15] = new PropertyDescriptor("zpos", Out.class);

            props[16] = new PropertyDescriptor("Y", Out.class,
                    "getY", "setY");

            props[17] = new PropertyDescriptor("dims", Out.class);
            props[18] = new PropertyDescriptor("spacings", Out.class);
            props[19] = new PropertyDescriptor("niduscount", Out.class);
            props[20] = new PropertyDescriptor("mnicoord", Out.class);
            props[21] = new PropertyDescriptor("zscore", Out.class);
            props[22] = new PropertyDescriptor("size", Out.class);
            props[23] = new PropertyDescriptor("data", Out.class);
            props[24] = new PropertyDescriptor("tvalue", Out.class);
            props[25] = new PropertyDescriptor("count", Out.class);
            props[26] = new PropertyDescriptor("rois", Out.class);
            props[27] = new PropertyDescriptor("sourcefile", Out.class);

            return props;

        } catch (IntrospectionException e) {
            e.printStackTrace();
        }
        return null;
    }
}
