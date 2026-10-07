package com.dacraezy1.subscript.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.dacraezy1.subscript.data.model.BillingCycle
import com.dacraezy1.subscript.data.model.Subscription
import com.dacraezy1.subscript.data.repository.SubscriptionRepository
import com.dacraezy1.subscript.ui.util.DateUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * UI State for the Subscription Dashboard.
 */
data class DashboardUiState(
    val subscriptions: List<Subscription> = emptyList(),
    val totalMonthlyCostByCurrency: Map<String, Double> = emptyMap(),
    val upcomingCountIn7Days: Int = 0,
    val isLoading: Boolean = false
)

/**
 * State for the Add/Edit Subscription form with field-level validation errors.
 */
data class AddEditFormState(
    val isOpen: Boolean = false,
    val isEditing: Boolean = false,
    val editingId: Int = 0,
    val name: String = "",
    val nameError: String? = null,
    val cost: String = "",
    val costError: String? = null,
    val currency: String = "$",
    val billingCycle: String = "Monthly",
    val nextDueDate: Long = DateUtils.getTomorrowStartMillis(),
    val isNotificationEnabled: Boolean = true
) {
    val isValid: Boolean
        get() = nameError == null && costError == null && name.isNotBlank() && cost.isNotBlank()
}

/**
 * ViewModel managing local subscription data and Add/Edit form state.
 */
class SubscriptionViewModel(
    private val repository: SubscriptionRepository
) : ViewModel() {

    private val _formState = MutableStateFlow(AddEditFormState())
    val formState: StateFlow<AddEditFormState> = _formState

    val dashboardState: StateFlow<DashboardUiState> = repository.allSubscriptions
        .combine(_formState) { subscriptions, _ ->
            // Calculate recurring monthly costs grouped by currency
            val monthlyCostByCurrency = mutableMapOf<String, Double>()
            var countIn7Days = 0

            subscriptions.forEach { sub ->
                val current = monthlyCostByCurrency.getOrDefault(sub.currency, 0.0)
                monthlyCostByCurrency[sub.currency] = current + sub.monthlyEquivalentCost

                val daysUntil = DateUtils.getDaysUntil(sub.nextDueDate)
                if (daysUntil in 0..7) {
                    countIn7Days++
                }
            }

            DashboardUiState(
                subscriptions = subscriptions,
                totalMonthlyCostByCurrency = monthlyCostByCurrency,
                upcomingCountIn7Days = countIn7Days,
                isLoading = false
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DashboardUiState(isLoading = true)
        )

    fun openAddDialog() {
        _formState.value = AddEditFormState(
            isOpen = true,
            isEditing = false,
            nextDueDate = DateUtils.getTomorrowStartMillis()
        )
    }

    fun openEditDialog(subscription: Subscription) {
        _formState.value = AddEditFormState(
            isOpen = true,
            isEditing = true,
            editingId = subscription.id,
            name = subscription.name,
            cost = if (subscription.cost % 1.0 == 0.0) {
                subscription.cost.toInt().toString()
            } else {
                subscription.cost.toString()
            },
            currency = subscription.currency,
            billingCycle = subscription.billingCycle,
            nextDueDate = subscription.nextDueDate,
            isNotificationEnabled = subscription.isNotificationEnabled
        )
    }

    fun closeDialog() {
        _formState.value = _formState.value.copy(isOpen = false)
    }

    fun onNameChange(name: String) {
        val error = when {
            name.isBlank() -> "Subscription name is required"
            name.length > 50 -> "Name cannot exceed 50 characters"
            else -> null
        }
        _formState.value = _formState.value.copy(name = name, nameError = error)
    }

    fun onCostChange(cost: String) {
        val sanitized = cost.filter { it.isDigit() || it == '.' }
        val error = when {
            sanitized.isBlank() -> "Cost is required"
            sanitized.toDoubleOrNull() == null -> "Please enter a valid amount"
            (sanitized.toDoubleOrNull() ?: 0.0) <= 0.0 -> "Cost must be greater than 0"
            else -> null
        }
        _formState.value = _formState.value.copy(cost = sanitized, costError = error)
    }

    fun onCurrencyChange(currency: String) {
        _formState.value = _formState.value.copy(currency = currency)
    }

    fun onBillingCycleChange(billingCycle: String) {
        _formState.value = _formState.value.copy(billingCycle = billingCycle)
    }

    fun onNextDueDateChange(dueDateMillis: Long) {
        _formState.value = _formState.value.copy(nextDueDate = dueDateMillis)
    }

    fun onNotificationToggle(enabled: Boolean) {
        _formState.value = _formState.value.copy(isNotificationEnabled = enabled)
    }

    fun saveSubscription(): Boolean {
        val state = _formState.value

        // Validate all fields before submission
        val name = state.name.trim()
        val nameError = if (name.isBlank()) "Subscription name is required" else null

        val costValue = state.cost.toDoubleOrNull()
        val costError = when {
            state.cost.isBlank() -> "Cost is required"
            costValue == null -> "Please enter a valid amount"
            costValue <= 0.0 -> "Cost must be greater than 0"
            else -> null
        }

        if (nameError != null || costError != null || costValue == null) {
            _formState.value = state.copy(
                nameError = nameError,
                costError = costError
            )
            return false
        }

        val subscription = Subscription(
            id = if (state.isEditing) state.editingId else 0,
            name = name,
            cost = costValue,
            currency = state.currency,
            billingCycle = state.billingCycle,
            nextDueDate = state.nextDueDate,
            isNotificationEnabled = state.isNotificationEnabled
        )

        viewModelScope.launch {
            if (state.isEditing) {
                repository.updateSubscription(subscription)
            } else {
                repository.addSubscription(subscription)
            }
        }

        closeDialog()
        return true
    }

    fun deleteSubscription(subscription: Subscription) {
        viewModelScope.launch {
            repository.deleteSubscription(subscription)
        }
    }

    class Factory(private val repository: SubscriptionRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(SubscriptionViewModel::class.java)) {
                return SubscriptionViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
