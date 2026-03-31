package com.voxable.feature_currency.domain.usecase

import com.voxable.core.util.Resource
import com.voxable.feature_currency.domain.repository.CurrencyRepository
import javax.inject.Inject

class ConvertCurrencyUseCase @Inject constructor(
    private val repository: CurrencyRepository
) {
    suspend operator fun invoke(amount: Double, from: String, to: String): Resource<Double> {
        if (amount <= 0) return Resource.Error("Miktar sıfırdan büyük olmalıdır")
        if (from == to) return Resource.Success(amount)
        return repository.convert(amount, from, to)
    }
}
