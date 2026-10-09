package com.wakana.vefa;

import com.wakana.vefa.model.SubscriptionPlanType;
import com.wakana.vefa.model.SubscriptionPlans;
import com.wakana.vefa.repository.SubscriptionPlanTypeRepository;
import com.wakana.vefa.repository.SubscriptionsPlanRepository;
import com.wakana.vefa.services.FeeService;
import com.wakana.vefa.services.SubscriptionPlanService;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;

import java.io.File;
import java.util.List;

@SpringBootApplication
@EnableTransactionManagement
@OpenAPIDefinition(info = @Info(title = "VEFA", version = "1.0.0", description = "", termsOfService = "runcodeNow", contact = @Contact(name = "Wakana", email = "contactwakana@gmail.com"), license = @License(name = "Licence", url = "Solimus")), security = {
        @SecurityRequirement(name = "bearerAuth") })
@SecurityScheme(name = "bearerAuth", type = SecuritySchemeType.HTTP, scheme = "bearer", bearerFormat = "JWT")
@ComponentScan(basePackages = { "com.wakana.vefa.services" })
@EnableCaching
public class VefaApplication implements CommandLineRunner {

    private final SubscriptionPlanService subscriptionPlanService;
    private final FeeService feeService;

    private final SubscriptionsPlanRepository plansRepository;
    private final SubscriptionPlanTypeRepository typeRepository;

    public VefaApplication(
            SubscriptionPlanService subscriptionPlanService,
            FeeService feeService,
            SubscriptionsPlanRepository plansRepository,
            SubscriptionPlanTypeRepository typeRepository) {

        this.subscriptionPlanService = subscriptionPlanService;
        this.feeService = feeService;
        this.plansRepository = plansRepository;
        this.typeRepository = typeRepository;
    }

    public static void main(String[] args) {
        SpringApplication.run(VefaApplication.class, args);
    }

    @Override
    public void run(String... args) {

        // 📁 Création du dossier files
        File directory = new File("files");
        if (!directory.exists()) {
            directory.mkdirs();
        }

        // 🔄 MIGRATION : label → SubscriptionPlanType
        List<SubscriptionPlans> plans = plansRepository.findAll();

        for (SubscriptionPlans plan : plans) {

            if (plan.getPlanType() != null) {
                continue;
            }

            String oldLabel = plan.getLabel();
            if (oldLabel == null || oldLabel.isBlank()) {
                continue;
            }

            SubscriptionPlanType planType = typeRepository
                    .findByLabel(oldLabel)
                    .orElseGet(() -> {
                        SubscriptionPlanType type = new SubscriptionPlanType();
                        type.setLabel(oldLabel);
                        return typeRepository.save(type);
                    });

            plan.setPlanType(planType);
            plansRepository.save(plan);
        }

        System.out.println("✅ Migration des SubscriptionPlans vers SubscriptionPlanType terminée");
    }
}
