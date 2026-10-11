
package com.wakana.vefa.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.wakana.vefa.dto.response.InvoiceResponseDto;
import com.wakana.vefa.dto.response.MonthlyRevenueDTO;
import com.wakana.vefa.dto.response.MonthlyRevenueResponseDTO;
import com.wakana.vefa.dto.response.MonthlyStatsDTO;
import com.wakana.vefa.dto.response.SubscriptionKpiDto;
import com.wakana.vefa.dto.response.SubscriptionPlanPercentageDTO;
import com.wakana.vefa.enums.ProfilEnum;
import com.wakana.vefa.model.Subscriptions;
import com.wakana.vefa.model.User;
import com.wakana.vefa.repository.UserRepository;
import com.wakana.vefa.services.InvoiceService;
import com.wakana.vefa.services.PaymentHistoryService;
import com.wakana.vefa.services.SubscriptionServices;
import com.wakana.vefa.util.ProfilEnumUtils;

import lombok.RequiredArgsConstructor;

import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;

@RestController
@RequestMapping("/api/user-subscriptions")
@RequiredArgsConstructor
public class SubscriptionsController {

    private final SubscriptionServices subscriptionService;
    private final UserRepository userRepository;
    private final InvoiceService invoiceService;
    private final PaymentHistoryService paymentHistoryService;

    /**
     * ✅ Créer un abonnement
     */
    @GetMapping("/create/{userId}/{planId}/{months}")
    @PreAuthorize("hasAuthority('PROMOTEUR')")
    public ResponseEntity<Void> createSubscription(
            @PathVariable Long userId,
            @PathVariable Long planId,
            @PathVariable int months,
            @RequestParam(required = false) String num_transaction_from_gu,
            @RequestParam(required = false) String num_command,
            @RequestParam(required = false) Double amount,
            @RequestParam(required = false) String errorCode) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        boolean paymentOk = "200".equals(errorCode);

        String redirectUrl = "https://solimus.sn/#/settings/accounts";
        

        if (paymentOk) {
            Subscriptions subscription = subscriptionService.createSubscription(user, planId, months);
            if (subscription != null) {
                invoiceService.generateInvoice(
                        user,
                        subscription,
                        subscription.getPaidAmount(),
                        num_transaction_from_gu);

                paymentHistoryService.markAsPaid(userId, planId);
            }

            return ResponseEntity
                    .status(HttpStatus.FOUND) // 302
                    .header("Location", redirectUrl)
                    .build();

        } else {
            return ResponseEntity
                    .status(HttpStatus.FOUND)
                    .header("Location", redirectUrl + "?payment=error")
                    .build();
        }
    }

    /**
     * 🔄 Renouveler un abonnement
     */
    /*
     * @PutMapping("/{subscriptionId}/renew")
     * public ResponseEntity<Subscription> renewSubscription(
     * 
     * @PathVariable Long subscriptionId,
     * 
     * @RequestParam(defaultValue = "1") int months) {
     * return
     * ResponseEntity.ok(subscriptionService.renewSubscription(subscriptionId,
     * months));
     * }
     */

    /**
     * 💳 Payer une échéance (paiement partiel)
     */
    /*
     * @PutMapping("/{subscriptionId}/pay")
     * public ResponseEntity<Subscription> payInstallment(
     * 
     * @PathVariable Long subscriptionId,
     * 
     * @RequestParam double amount) {
     * return ResponseEntity.ok(subscriptionService.payInstallment(subscriptionId,
     * amount));
     * }
     */

    /**
     * ✅ Vérifier si l’abonnement est actif pour un utilisateur
     */
    @GetMapping("/is-active/{userId}/{profil}")
    @PreAuthorize("hasAuthority('PROMOTEUR')")
    public ResponseEntity<Boolean> isSubscriptionActive(@PathVariable Long userId, @PathVariable String profil) {
        return ResponseEntity.ok(subscriptionService.isSubscriptionActive(userId, profil));
    }

    @GetMapping("/me/is-active")
    @PreAuthorize("hasAuthority('PROMOTEUR')")
    public ResponseEntity<Boolean> isMySubscriptionActive(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(subscriptionService.isSubscriptionActive(user.getId(), user.getProfil().name()));
    }

    /**
     * 🏗️ Vérifier si l’utilisateur peut créer un projet
     */
    @GetMapping("/can-create-project/{userId}/{profil}")
    @PreAuthorize("hasAuthority('PROMOTEUR')")
    public ResponseEntity<Boolean> canCreateProject(@PathVariable Long userId, @PathVariable String profil) {
        return ResponseEntity.ok(subscriptionService.canCreateProject(userId, profil));
    }

    @GetMapping("/me/can-create-project")
    @PreAuthorize("hasAuthority('PROMOTEUR')")
    public ResponseEntity<Boolean> canCreateMyProject(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(subscriptionService.canCreateProject(user.getId(), user.getProfil().name()));
    }

    /**
     * 🗂️ Lister tous les abonnements
     */
    @GetMapping
    public ResponseEntity<List<Subscriptions>> getAllSubscriptions() {
        return ResponseEntity.ok(subscriptionService.getAllSubscriptions());
    }

    /**
     * 📅 Lister les abonnements d’un utilisateur (triés par date de fin)
     */
    /*
     * @GetMapping("/user/{userId}")
     * public Subscriptions getUserSubscription(@PathVariable Long userId) {
     * return subscriptionService.getSubscriptionByUserId(userId);
     * }
     */
    /*
     * * @GetMapping("/user/{userId}")
     * public ResponseEntity<Page<Subscription>> getUserSubscriptions(
     * 
     * @PathVariable Long userId,
     * Pageable pageable) {
     * return
     * ResponseEntity.ok(subscriptionService.getUserSubscriptionsOrderedByEndDate(
     * userId, pageable));
     * }
     */

    /*
     * @GetMapping("/invoices/{userId}")
     * public ResponseEntity<Page<InvoiceResponseDto>> getUserInvoices(
     * 
     * @PathVariable Long userId,
     * 
     * @RequestParam(defaultValue = "0") int page,
     * 
     * @RequestParam(defaultValue = "10") int size) {
     * 
     * Page<InvoiceResponseDto> invoices = invoiceService
     * .getInvoicesByUser(userId, PageRequest.of(page, size))
     * .map(InvoiceResponseDto::fromEntity);
     * 
     * return ResponseEntity.ok(invoices);
     * }
     */

    @GetMapping("/invoices/{userId}/plan/{planName}")
    public ResponseEntity<Page<InvoiceResponseDto>> getUserInvoicesByPlan(
            @PathVariable Long userId,
            @PathVariable String planName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        ProfilEnum profilEnum = ProfilEnumUtils.toProfilEnum(planName);

        Page<InvoiceResponseDto> invoices = invoiceService
                .getInvoicesByUserAndPlan(
                        userId,
                        profilEnum,
                        PageRequest.of(page, size))
                .map(InvoiceResponseDto::fromEntity);

        return ResponseEntity.ok(invoices);
    }

    @GetMapping("/dashbord")
    public SubscriptionKpiDto getSubscriptionKpis() {
        return subscriptionService.getSubscriptionKpis();
    }

    @GetMapping("/evolution")
    public List<MonthlyStatsDTO> getEvolution(@RequestParam(required = false) Integer year) {
        return subscriptionService.getSubscriptionEvolution(year);
    }

    @GetMapping("/revenues/evolution")
    public MonthlyRevenueResponseDTO getRevenueEvolution(
            @RequestParam(required = false) Integer year) {
        return invoiceService.getMonthlyRevenueEvolution(year);
    }

    @GetMapping("/plan-distribution")
    public ResponseEntity<List<SubscriptionPlanPercentageDTO>> getSubscriptionPlanDistribution() {
        return ResponseEntity.ok(subscriptionService.getSubscriptionPlanDistribution());
    }

    @GetMapping("/invoices-lastest")
    public Page<InvoiceResponseDto> getInvoices(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return invoiceService.getInvoices(pageable);
    }

    @GetMapping("/user/{userId}")
    public List<Subscriptions> getUserSubscriptions(
            @PathVariable Long userId) {
        return subscriptionService.getUserSubscriptions(userId);
    }

    // 🔹 2) Abonnement d’un user par nom de plan
    @GetMapping("/user/{userId}/plan/{planName}")
    public Subscriptions getUserSubscriptionByPlan(
            @PathVariable Long userId,
            @PathVariable String planName) {
        ProfilEnum profilEnum = ProfilEnum.valueOf(planName.toUpperCase());
        return subscriptionService.getUserSubscriptionByPlan(userId, profilEnum);
    }
}
