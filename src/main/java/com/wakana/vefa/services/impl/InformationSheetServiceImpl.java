package com.wakana.vefa.services.impl;

import com.wakana.vefa.dto.InformationSheetRequest;
import com.wakana.vefa.model.InformationSheet;
import com.wakana.vefa.model.User;
import com.wakana.vefa.repository.InformationSheetRepository;
import com.wakana.vefa.repository.UserRepository;
import com.wakana.vefa.services.InformationSheetService;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InformationSheetServiceImpl implements InformationSheetService {

    private final InformationSheetRepository informationSheetRepository;
    private final UserRepository userRepository;

    @Override
    public InformationSheet uploadInformationSheet(InformationSheetRequest request) throws IOException {
        // Retrieve the notary (user)
        User notary = userRepository.findById(request.getNotaryId())
                .orElseThrow(() -> new EntityNotFoundException("Notary not found"));

        InformationSheet informationSheet = new InformationSheet();
        informationSheet.setTitle(request.getTitle());
        informationSheet.setFile(request.getFileName());
        informationSheet.setNotary(notary);
        return informationSheetRepository.save(informationSheet);
    }

    @Override
    public List<InformationSheet> getInformationSheetsByNotary(Long notaryId) {
        return informationSheetRepository.findByNotaryId(notaryId);
    }

    @Override
    public InformationSheet getInformationSheet(Long id) {
        return informationSheetRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Information sheet not found"));
    }

    @Override
    public void deleteInformationSheet(Long id) {
        if (!informationSheetRepository.existsById(id)) {
            throw new EntityNotFoundException("Information sheet not found");
        }
        informationSheetRepository.deleteById(id);
    }
}
