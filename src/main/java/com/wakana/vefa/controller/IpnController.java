package com.wakana.vefa.controller;
import java.time.LocalDateTime;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.wakana.vefa.enums.PaymentCallStatusEnum;
import com.wakana.vefa.model.CallForCharge;
import com.wakana.vefa.model.FoundHistory;
import com.wakana.vefa.model.RentalInvoice;
import com.wakana.vefa.model.User;
import com.wakana.vefa.repository.CallForChargeRepository;
import com.wakana.vefa.repository.FoundHistoryRepository;
import com.wakana.vefa.repository.RentalInvoiceRepository;
import com.wakana.vefa.repository.UserRepository;
import com.wakana.vefa.services.RealEstatePropertyService;
import com.wakana.vefa.services.SubscriptionService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/ipn")
@RequiredArgsConstructor
public class IpnController {
    private final SubscriptionService subscriptionService;
    private final RealEstatePropertyService realEstatePropertyService;
    private final UserRepository userRepository;
    private final FoundHistoryRepository foundHistoryRepository;
    private final CallForChargeRepository callForChargeRepository;
    private final RentalInvoiceRepository rentalInvoiceRepository;
    

    @PostMapping("/{subscriptionId}/{installmentCount}/ipn")
    public ResponseEntity<?> getIpn(@PathVariable Long subscriptionId,
            @PathVariable int installmentCount) {

        subscriptionService.processPayment(subscriptionId, installmentCount);

        String message = "Félicitations ! Votre paiement est validé avec succès.";
        return ResponseEntity.ok(message);

    }

    @PostMapping("/founds/{id}/{founds}/ipn")
    public ResponseEntity<?> getIpnLotFee(@PathVariable Long id,@PathVariable double founds) {
        User user =userRepository.findById(id).orElse(null);
        FoundHistory foundHistory =new FoundHistory();
        foundHistory.setAmount(founds);
        foundHistory.setTitle("Chargement de fonds");
        foundHistory.setUser(user);
        foundHistory.setType(0);
        foundHistory.setLastUpdated(LocalDateTime.now());
        if(user != null){
            user.setFunds(founds);
            foundHistory.setPayed(true);
            userRepository.save(user);
        }

        foundHistoryRepository.save( foundHistory);
        String message = "Félicitations ! Votre paiement est validé avec succès.";
        return ResponseEntity.ok(message);

    }
    
    
    @PostMapping("/call-for-charges/{id}/{founds}/ipn")
    public ResponseEntity<?> getIpnCallForCharge(@PathVariable Long id,@PathVariable double founds) {
      CallForCharge callForCharge = callForChargeRepository.findById(id).orElse(null);
        
        FoundHistory foundHistory =new FoundHistory();
        foundHistory.setAmount(founds);
        foundHistory.setType(1);
        foundHistory.setTitle(callForCharge.getLabel());
        foundHistory.setUser(callForCharge.getOwner());
        foundHistory.setLastUpdated(LocalDateTime.now());
        foundHistory.setPayed(true);
        callForCharge.setStatus(PaymentCallStatusEnum.PAID);
        foundHistoryRepository.save( foundHistory);
        String message = "Félicitations ! Votre paiement est validé avec succès.";
        return ResponseEntity.ok(message);

    }

    @PostMapping("/lotfee/{id}/ipn")
    public ResponseEntity<?> getIpnFounds(@PathVariable Long id) {
        realEstatePropertyService.payLotFee(id);
        String message = "Félicitations ! Votre paiement est validé avec succès.";
        return ResponseEntity.ok(message);

    }


    @PostMapping("/rental/{id}/{founds}/ipn")
    public ResponseEntity<?> rental(@PathVariable Long id,@PathVariable double founds) {
      RentalInvoice rentalInvoice = rentalInvoiceRepository.findById(id).orElse(null);
        
        FoundHistory foundHistory =new FoundHistory();
        foundHistory.setAmount(founds);
        foundHistory.setType(3);
        foundHistory.setTitle("Rental Invoice N°"+rentalInvoice.getId());
        foundHistory.setUser(rentalInvoice.getProperty().getRecipient());
        foundHistory.setRentalInvoice(rentalInvoice);
        foundHistory.setLastUpdated(LocalDateTime.now());
        foundHistory.setPayed(true);
        rentalInvoice.setPaid(true);
        rentalInvoiceRepository.save(rentalInvoice);
        String message = "Félicitations ! Votre paiement est validé avec succès.";
        return ResponseEntity.ok(message);

    }



    @GetMapping("/success")
    public ResponseEntity<?> getSuccess() {

        String message = "Félicitations ! Votre paiement est validé avec succès.";

        return ResponseEntity.ok(message);

    }

    @GetMapping("/cancel")
    public ResponseEntity<?> getCancel() {
        return ResponseEntity.ok(false);

    }

    @GetMapping("/deeplink")
    public String deepLink() {
        // Générer le lien profond avec les paramètres
        String deepLink = "peey://action?id=" + 3;
        // Rediriger l'utilisateur vers le lien profond
        return "redirect:" + deepLink;
    }

}
