package com.demospringfullstack.springbootexample.customer;

import com.demospringfullstack.springbootexample.exception.DuplicateResourceException;
import com.demospringfullstack.springbootexample.exception.RequestValidationException;
import com.demospringfullstack.springbootexample.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerDAO customerDAO;
    private CustomerService underTest;

    @BeforeEach
    void setUp() {
        underTest = new CustomerService(customerDAO);
    }

    @Test
    void getAllCustomers() {
        // When
        underTest.getAllCustomers();
        // Then
        verify(customerDAO).selectAllCustomer();
    }

    @Test
    void canGetCustomer() {
        // Given
        int id = 10;
        Customer customer = new Customer(
                id, "John", Gender.MALE, "john@mailservice.com", 22
        );
        // if this works returns optional
        when(customerDAO.selectCustomerById(id)).thenReturn(Optional.of(customer));

        // When
        Customer actual = underTest.getCustomer(id);

        // Then
        assertThat(actual).isEqualTo(customer);
    }

    @Test
    void willThrowWhenGetCustomerReturnEmptyOptional() {
        // Given
        int id = 10;
        Customer customer = new Customer(
                id, "John", Gender.MALE, "john@mailservice.com", 22
        );
        // if this works returns optional
        when(customerDAO.selectCustomerById(id)).thenReturn(Optional.empty());

        // When
        // Then
        assertThatThrownBy(() -> underTest.getCustomer(id))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("customer with id [%s] not found".formatted(id));
    }

    @Test
    void addCustomer() {
        // Given
        String email = "john@mailservice.com";

        when(customerDAO.existsPersonWithEmail(email)).thenReturn(false);

        var request = new CustomerRegistrationRequest(
           "John", email, 21, Gender.MALE
        );

        // When
        underTest.addCustomer(request);

        // Then
        ArgumentCaptor<Customer> customerArgumentCaptor = ArgumentCaptor.forClass(
                Customer.class
        );
        verify(customerDAO).insertCustomer(customerArgumentCaptor.capture());

        Customer capturedCustomer = customerArgumentCaptor.getValue();

        assertThat(capturedCustomer.getId()).isNull();
        assertThat(capturedCustomer.getName().equals(request.name()));
        assertThat(capturedCustomer.getEmail().equals(request.email()));
        assertThat(capturedCustomer.getAge().equals(request.age()));
    }

    @Test
    void willThrowWhenEmailExistsWhileAddingCustomer() {
        // Given
        String email = "john@mailservice.com";

        when(customerDAO.existsPersonWithEmail(email)).thenReturn(true);

        var request = new CustomerRegistrationRequest(
                "John", email, 21, Gender.MALE
        );

        // When
        assertThatThrownBy(() -> underTest.addCustomer(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage("email already exists");

        // Then
        verify(customerDAO, never()).insertCustomer(any());
    }

    @Test
    void removeCustomerById() {
        // Given
        int id = 10;
        when(customerDAO.existsPersonWithId(id)).thenReturn(true);

        // When
        underTest.removeCustomerById(id);

        // Then
        verify(customerDAO).removeCustomerById(id);
    }

    @Test
    void willThrowWhenIdNotExistsWhileRemoveCustomerById() {
        // Given
        int id = 10;
        when(customerDAO.existsPersonWithId(id)).thenReturn(false);

        // When
        assertThatThrownBy(() -> underTest.removeCustomerById(id))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("customer with id [%s] not found".formatted(id));

        // Then
        verify(customerDAO, never()).removeCustomerById(any());
    }

    @Test
    void canUpdateAllCustomersProperties() {
        // Given
        int id = 10;
        String newEmail = "johnbolt@mailingservice.com";
        var request = new CustomerUpdateRequest(
                "John Bolt", newEmail, 33
        );
        Customer customer = new Customer(
                id, "John", Gender.MALE, "john@mailservice.com", 22
        );

        when(customerDAO.selectCustomerById(id)).thenReturn(Optional.of(customer));
        when(customerDAO.existsPersonWithEmail(newEmail)).thenReturn(false);

        // When
        underTest.updateCustomer(id, request);

        // Then
        ArgumentCaptor<Customer> customerArgumentCaptor = ArgumentCaptor.forClass(
                Customer.class
        );
        verify(customerDAO).updateCustomer(customerArgumentCaptor.capture());
        Customer capturedCustomer = customerArgumentCaptor.getValue();

        assertThat(capturedCustomer.getName()).isEqualTo(request.name());
        assertThat(capturedCustomer.getEmail()).isEqualTo(request.email());
        assertThat(capturedCustomer.getAge()).isEqualTo(request.age());
    }

    @Test
    void canUpdateOnlyCustomerName() {
        // Given
        int id = 10;
        var request = new CustomerUpdateRequest(
                "John Bolt", null, null
        );
        Customer customer = new Customer(
                id, "John", Gender.MALE, "john@mailservice.com", 22
        );

        when(customerDAO.selectCustomerById(id)).thenReturn(Optional.of(customer));

        // When
        underTest.updateCustomer(id, request);

        // Then
        ArgumentCaptor<Customer> customerArgumentCaptor = ArgumentCaptor.forClass(
                Customer.class
        );
        verify(customerDAO).updateCustomer(customerArgumentCaptor.capture());
        Customer capturedCustomer = customerArgumentCaptor.getValue();

        assertThat(capturedCustomer.getName()).isEqualTo(request.name());
        assertThat(capturedCustomer.getEmail()).isEqualTo(customer.getEmail());
        assertThat(capturedCustomer.getAge()).isEqualTo(customer.getAge());
    }

    @Test
    void canUpdateOnlyCustomerEmail() {
        // Given
        int id = 10;
        String newEmail = "johnbolt@mailingservice.com";
        var request = new CustomerUpdateRequest(
                null, newEmail, null
        );
        Customer customer = new Customer(
                id, "John", Gender.MALE, "john@mailservice.com", 22
        );

        when(customerDAO.selectCustomerById(id)).thenReturn(Optional.of(customer));
        when(customerDAO.existsPersonWithEmail(newEmail)).thenReturn(false);

        // When
        underTest.updateCustomer(id, request);

        // Then
        ArgumentCaptor<Customer> customerArgumentCaptor = ArgumentCaptor.forClass(
                Customer.class
        );
        verify(customerDAO).updateCustomer(customerArgumentCaptor.capture());
        Customer capturedCustomer = customerArgumentCaptor.getValue();

        assertThat(capturedCustomer.getName()).isEqualTo(customer.getName());
        assertThat(capturedCustomer.getEmail()).isEqualTo(request.email());
        assertThat(capturedCustomer.getAge()).isEqualTo(customer.getAge());
    }

    @Test
    void canUpdateOnlyCustomerAge() {
        // Given
        int id = 10;
        var request = new CustomerUpdateRequest(
                null, null, 33
        );
        Customer customer = new Customer(
                id, "John", Gender.MALE, "john@mailservice.com", 22
        );

        when(customerDAO.selectCustomerById(id)).thenReturn(Optional.of(customer));

        // When
        underTest.updateCustomer(id, request);

        // Then
        ArgumentCaptor<Customer> customerArgumentCaptor = ArgumentCaptor.forClass(
                Customer.class
        );
        verify(customerDAO).updateCustomer(customerArgumentCaptor.capture());
        Customer capturedCustomer = customerArgumentCaptor.getValue();

        assertThat(capturedCustomer.getName()).isEqualTo(customer.getName());
        assertThat(capturedCustomer.getEmail()).isEqualTo(customer.getEmail());
        assertThat(capturedCustomer.getAge()).isEqualTo(request.age());
    }

    @Test
    void willThrowWhenUpdateCustomerReturnEmptyOptional() {
        // Given
        int id = 10;
        boolean changes = false;
        var request = new CustomerUpdateRequest(
                "John Bolt", "john@mailingservice.com", 33
        );

        when(customerDAO.selectCustomerById(id)).thenReturn(Optional.empty());

        // When
        // Then
        assertThatThrownBy(() -> underTest.updateCustomer(id, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("customer with id [%s] not found".formatted(id));

        verify(customerDAO, never()).updateCustomer(any());
    }

    @Test
    void willThrowWhenEmailExistsWhileUpdateCustomer() {
        // Given
        int id = 10;
        // boolean changes = false;
        String newEmail = "johnny@mailservice.com";
        var request = new CustomerUpdateRequest(
                "John Bolt", newEmail, 33
        );
        Customer customer = new Customer(
                id, "John", Gender.MALE, "john@mailservice.com", 22
        );
        Customer johnny = new Customer(
                1, "Johnny", Gender.MALE, "johnny@mailservice.com", 21
        );
        when(customerDAO.selectCustomerById(id)).thenReturn(Optional.of(customer));
        when(customerDAO.existsPersonWithEmail(newEmail)).thenReturn(true);

        // When
        assertThatThrownBy(() -> underTest.updateCustomer(id, request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage("email already taken");

        // Then
        verify(customerDAO, never()).updateCustomer(any());
    }

    @Test
    void willThrowWhenNoChangesWhileUpdateCustomer() {
        // Given
        int id = 10;
        Customer customer = new Customer(
                id, "John", Gender.MALE, "john@mailservice.com", 22
        );
        var request = new CustomerUpdateRequest(
                customer.getName(), customer.getEmail(), customer.getAge()
        );

        when(customerDAO.selectCustomerById(id)).thenReturn(Optional.of(customer));

        // When
        assertThatThrownBy(() -> underTest.updateCustomer(id, request))
                .isInstanceOf(RequestValidationException.class)
                .hasMessage("No data changes found");

        // Then
        verify(customerDAO, never()).updateCustomer(any());
    }
}