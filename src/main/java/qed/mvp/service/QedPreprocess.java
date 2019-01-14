package qed.mvp.service;

import com.mathworks.mps.client.MATLABException;
import com.mathworks.mps.client.annotations.MWStructureList;
import qed.mvp.entity.Out;
import qed.mvp.entity.Params;

import java.io.IOException;

public interface QedPreprocess {
    @MWStructureList({Params.class, Out.class})
    Out qed_preprocess(Params params) throws IOException, MATLABException;

}
