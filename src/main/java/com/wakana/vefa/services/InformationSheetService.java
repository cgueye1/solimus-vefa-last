package com.wakana.vefa.services;

import com.wakana.vefa.dto.InformationSheetRequest;
import com.wakana.vefa.model.InformationSheet;

import java.io.IOException;
import java.util.List;

public interface InformationSheetService {

    InformationSheet uploadInformationSheet(InformationSheetRequest request) throws IOException;

    List<InformationSheet> getInformationSheetsByNotary(Long notaryId);

    InformationSheet getInformationSheet(Long id);

    void deleteInformationSheet(Long id);
}
