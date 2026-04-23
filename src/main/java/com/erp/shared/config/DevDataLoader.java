package com.erp.shared.config;

import com.erp.core.module.ModuleCode;
import com.erp.core.organization.Organization;
import com.erp.core.organization.OrganizationRepository;
import com.erp.core.users.AppUser;
import com.erp.core.users.Role;
import com.erp.core.users.UserRepository;
import com.erp.modules.customers.Customer;
import com.erp.modules.customers.CustomerRepository;
import com.erp.modules.customers.CustomerType;
import com.erp.modules.orders.Order;
import com.erp.modules.orders.OrderRepository;
import com.erp.modules.orders.OrderStatus;
import com.erp.modules.expenses.Expense;
import com.erp.modules.expenses.ExpenseRepository;
import com.erp.modules.payments.Payment;
import com.erp.modules.payments.PaymentMethod;
import com.erp.modules.payments.PaymentRepository;
import com.erp.modules.reminders.ReminderRule;
import com.erp.modules.reminders.ReminderRuleRepository;
import com.erp.modules.reminders.NotificationChannel;
import com.erp.modules.reminders.ReminderTriggerType;
import com.erp.modules.sales.SalesOfficer;
import com.erp.modules.sales.SalesOfficerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.EnumSet;
import java.util.UUID;

@Component
@Profile("dev")
@RequiredArgsConstructor
public class DevDataLoader implements CommandLineRunner {

    private static final UUID ORG = UUID.fromString("f0000001-0001-0001-0001-000000000001");
    private static final UUID USER_ADMIN = UUID.fromString("f0000002-0002-0002-0002-000000000002");
    private static final UUID USER_MANAGER = UUID.fromString("f0000003-0003-0003-0003-000000000003");
    private static final UUID USER_SALES = UUID.fromString("f0000004-0004-0004-0004-000000000004");
    private static final UUID SALES_OFFICER = UUID.fromString("f0000005-0005-0005-0005-000000000005");
    private static final UUID CUSTOMER = UUID.fromString("f1000001-0001-0001-0001-000000000001");
    private static final UUID ORDER = UUID.fromString("f2000001-0001-0001-0001-000000000001");
    private static final UUID PAYMENT = UUID.fromString("f3000001-0001-0001-0001-000000000001");
    private static final UUID RULE1 = UUID.fromString("f4000001-0001-0001-0001-000000000001");
    private static final UUID RULE2 = UUID.fromString("f4000002-0002-0002-0002-000000000002");
    private static final UUID EXPENSE = UUID.fromString("f5000001-0001-0001-0001-000000000001");

    private final OrganizationRepository organizationRepository;
    private final UserRepository userRepository;
    private final SalesOfficerRepository salesOfficerRepository;
    private final CustomerRepository customerRepository;
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final ExpenseRepository expenseRepository;
    private final ReminderRuleRepository reminderRuleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        if (organizationRepository.existsById(ORG)) {
            return;
        }

        Organization org = new Organization();
        org.setId(ORG);
        org.setName("Acme Demo");
        org.setEnabledModules(EnumSet.allOf(ModuleCode.class));
        organizationRepository.save(org);

        String hash = passwordEncoder.encode("password");
        saveUser(USER_ADMIN, ORG, "admin@acme.demo", hash, Role.ADMIN);
        saveUser(USER_MANAGER, ORG, "manager@acme.demo", hash, Role.MANAGER);
        saveUser(USER_SALES, ORG, "sales@acme.demo", hash, Role.SALES_OFFICER);

        SalesOfficer so = new SalesOfficer();
        so.setId(SALES_OFFICER);
        so.setOrgId(ORG);
        so.setName("Field Rep");
        so.setUserId(USER_SALES);
        so.setTerritory("North");
        salesOfficerRepository.save(so);

        Customer c = new Customer();
        c.setId(CUSTOMER);
        c.setOrgId(ORG);
        c.setName("Globex Retail");
        c.setType(CustomerType.RETAILER);
        c.setPhone("+10000000001");
        c.setWhatsappNumber("+10000000001");
        c.setEmail("billing@globex.example");
        c.setAddress("1 Market St");
        c.setCreditLimit(new BigDecimal("50000.0000"));
        c.setPaymentTermsDays(30);
        c.setAssignedSalesOfficerId(SALES_OFFICER);
        customerRepository.save(c);

        Order o = new Order();
        o.setId(ORDER);
        o.setOrgId(ORG);
        o.setCustomerId(CUSTOMER);
        o.setOrderDate(LocalDate.of(2026, 3, 19));
        o.setDueDate(LocalDate.of(2026, 4, 18));
        o.setTotalAmount(new BigDecimal("10000.0000"));
        o.setStatus(OrderStatus.PARTIAL);
        orderRepository.save(o);

        Payment p = new Payment();
        p.setId(PAYMENT);
        p.setOrgId(ORG);
        p.setOrderId(ORDER);
        p.setAmount(new BigDecimal("3500.0000"));
        p.setPaymentDate(LocalDate.of(2026, 4, 10));
        p.setMethod(PaymentMethod.BANK_TRANSFER);
        paymentRepository.save(p);

        ReminderRule r1 = new ReminderRule();
        r1.setId(RULE1);
        r1.setOrgId(ORG);
        r1.setTriggerType(ReminderTriggerType.BEFORE_DUE);
        r1.setDaysOffset(-2);
        r1.setChannel(NotificationChannel.EMAIL);
        reminderRuleRepository.save(r1);

        ReminderRule r2 = new ReminderRule();
        r2.setId(RULE2);
        r2.setOrgId(ORG);
        r2.setTriggerType(ReminderTriggerType.AFTER_DUE);
        r2.setDaysOffset(1);
        r2.setChannel(NotificationChannel.SMS);
        reminderRuleRepository.save(r2);

        Expense e = new Expense();
        e.setId(EXPENSE);
        e.setOrgId(ORG);
        e.setUserId(USER_ADMIN);
        e.setAmount(new BigDecimal("120.5000"));
        e.setCategory("TRAVEL");
        e.setDescription("Client visit");
        e.setDate(LocalDate.of(2026, 4, 1));
        expenseRepository.save(e);
    }

    private void saveUser(UUID id, UUID orgId, String email, String hash, Role role) {
        AppUser u = new AppUser();
        u.setId(id);
        u.setOrgId(orgId);
        u.setEmail(email);
        u.setPasswordHash(hash);
        u.setRole(role);
        u.setEnabled(true);
        userRepository.save(u);
    }
}
