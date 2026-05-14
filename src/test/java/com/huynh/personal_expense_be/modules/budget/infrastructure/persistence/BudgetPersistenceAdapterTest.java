package com.huynh.personal_expense_be.modules.budget.infrastructure.persistence;

import jakarta.persistence.EntityManager;
import org.hibernate.Session;
import org.hibernate.jdbc.Work;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class BudgetPersistenceAdapterTest {

    @Mock
    private EntityManager entityManager;

    @Mock
    private Session session;

    @Mock
    private Connection connection;

    @Mock
    private PreparedStatement preparedStatement;

    private BudgetPersistenceAdapter adapter;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        adapter = new BudgetPersistenceAdapter(entityManager);
        when(entityManager.unwrap(any())).thenReturn(session);
    }

    @Test
    void testEnsureBudgetExists() throws Exception {
        String userId = "user-1";
        UUID categoryId = UUID.randomUUID();
        String period = "2026-05";

        doAnswer(invocation -> {
            Work work = invocation.getArgument(0);
            work.execute(connection);
            return null;
        }).when(session).doWork(any());

        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);

        adapter.ensureBudgetExists(userId, categoryId, period);

        verify(connection).prepareStatement(anyString());
        verify(preparedStatement).setObject(eq(1), any(UUID.class));
        verify(preparedStatement).setString(2, "2026-05");
        verify(preparedStatement).setString(3, "user-1");
        verify(preparedStatement).setObject(4, categoryId);
        verify(preparedStatement).setString(5, "2026-04");
        verify(preparedStatement).executeUpdate();
    }

    @Test
    void testIncrementSpentAmount() throws Exception {
        String userId = "user-1";
        UUID categoryId = UUID.randomUUID();
        String period = "2026-05";
        BigDecimal delta = new BigDecimal("50.00");

        doAnswer(invocation -> {
            Work work = invocation.getArgument(0);
            work.execute(connection);
            return null;
        }).when(session).doWork(any());

        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);

        int result = adapter.incrementSpentAmount(userId, categoryId, period, delta);

        verify(connection).prepareStatement(anyString());
        verify(preparedStatement).setBigDecimal(1, delta);
        verify(preparedStatement).setString(2, "user-1");
        verify(preparedStatement).setObject(3, categoryId);
        verify(preparedStatement).setString(4, "2026-05");
        verify(preparedStatement).executeUpdate();

        assertEquals(1, result);
    }

    @Test
    void testMarkBudgetExceededIfThresholdReached() throws Exception {
        String userId = "user-1";
        UUID categoryId = UUID.randomUUID();
        String period = "2026-05";

        doAnswer(invocation -> {
            Work work = invocation.getArgument(0);
            work.execute(connection);
            return null;
        }).when(session).doWork(any());

        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);

        int result = adapter.markBudgetExceededIfThresholdReached(userId, categoryId, period);

        verify(connection).prepareStatement(anyString());
        verify(preparedStatement).setString(1, userId);
        verify(preparedStatement).setObject(2, categoryId);
        verify(preparedStatement).setString(3, period);
        verify(preparedStatement).executeUpdate();
        assertEquals(1, result);
    }

    @Test
    void testExpireBudgetsBeforePeriod() throws Exception {
        String period = "2026-05";

        doAnswer(invocation -> {
            Work work = invocation.getArgument(0);
            work.execute(connection);
            return null;
        }).when(session).doWork(any());

        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(2);

        int result = adapter.expireBudgetsBeforePeriod(period);

        verify(connection).prepareStatement(anyString());
        verify(preparedStatement).setString(1, period);
        verify(preparedStatement).executeUpdate();
        assertEquals(2, result);
    }
}
