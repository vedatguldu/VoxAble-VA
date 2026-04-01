package com.voxable.feature_currency.data.local

import com.voxable.feature_currency.domain.model.Currency

object CurrencyDataProvider {

    private val currencies: List<Currency> = listOf(
        Currency(
            code = "TRY",
            name = "Türk Lirası",
            symbol = "₺",
            country = "Türkiye",
            banknoteKeywords = listOf(
                "TÜRKİYE", "TÜRK LİRASI", "CUMHURİYET", "MERKEZ BANKASI",
                "ATATÜRK", "BANKNOT", "TÜRK", "LIRA", "TL"
            ),
            denominations = listOf(5, 10, 20, 50, 100, 200)
        ),
        Currency(
            code = "USD",
            name = "Amerikan Doları",
            symbol = "$",
            country = "Amerika Birleşik Devletleri",
            banknoteKeywords = listOf(
                "UNITED STATES", "FEDERAL RESERVE", "DOLLAR", "IN GOD WE TRUST",
                "THE UNITED STATES OF AMERICA", "THIS NOTE IS LEGAL TENDER",
                "WASHINGTON", "FRANKLIN", "LINCOLN", "HAMILTON", "JACKSON", "GRANT"
            ),
            denominations = listOf(1, 2, 5, 10, 20, 50, 100)
        ),
        Currency(
            code = "EUR",
            name = "Euro",
            symbol = "€",
            country = "Avrupa Birliği",
            banknoteKeywords = listOf(
                "EURO", "EUR", "EUROPEAN CENTRAL BANK", "BANQUE CENTRALE EUROPEENNE",
                "EUROPÄISCHE ZENTRALBANK", "BANCA CENTRALE EUROPEA",
                "BANCO CENTRAL EUROPEO", "BCE", "ECB", "EZB"
            ),
            denominations = listOf(5, 10, 20, 50, 100, 200, 500)
        ),
        Currency(
            code = "GBP",
            name = "İngiliz Sterlini",
            symbol = "£",
            country = "Birleşik Krallık",
            banknoteKeywords = listOf(
                "BANK OF ENGLAND", "POUND", "STERLING", "PROMISE TO PAY",
                "ELIZABETH", "CHARLES", "UNITED KINGDOM", "ENGLAND"
            ),
            denominations = listOf(5, 10, 20, 50)
        ),
        Currency(
            code = "JPY",
            name = "Japon Yeni",
            symbol = "¥",
            country = "Japonya",
            banknoteKeywords = listOf(
                "日本銀行券", "NIPPON GINKO", "日本", "円", "YEN",
                "BANK OF JAPAN", "千円", "壱万円", "五千円", "二千円"
            ),
            denominations = listOf(1000, 2000, 5000, 10000)
        ),
        Currency(
            code = "CHF",
            name = "İsviçre Frangı",
            symbol = "CHF",
            country = "İsviçre",
            banknoteKeywords = listOf(
                "SCHWEIZERISCHE NATIONALBANK", "BANQUE NATIONALE SUISSE",
                "BANCA NAZIONALE SVIZZERA", "SWISS NATIONAL BANK",
                "FRANC", "FRANKEN", "FRANCO", "HELVETIA"
            ),
            denominations = listOf(10, 20, 50, 100, 200, 1000)
        ),
        Currency(
            code = "CAD",
            name = "Kanada Doları",
            symbol = "C$",
            country = "Kanada",
            banknoteKeywords = listOf(
                "BANK OF CANADA", "BANQUE DU CANADA", "CANADA", "DOLLAR",
                "CANADIAN"
            ),
            denominations = listOf(5, 10, 20, 50, 100)
        ),
        Currency(
            code = "AUD",
            name = "Avustralya Doları",
            symbol = "A$",
            country = "Avustralya",
            banknoteKeywords = listOf(
                "RESERVE BANK OF AUSTRALIA", "AUSTRALIA", "AUSTRALIAN",
                "DOLLAR", "COMMONWEALTH"
            ),
            denominations = listOf(5, 10, 20, 50, 100)
        ),
        Currency(
            code = "CNY",
            name = "Çin Yuanı",
            symbol = "¥",
            country = "Çin",
            banknoteKeywords = listOf(
                "中国人民银行", "ZHONGGUO RENMIN YINHANG", "人民币", "元",
                "PEOPLE'S BANK OF CHINA", "YUAN", "RENMINBI"
            ),
            denominations = listOf(1, 5, 10, 20, 50, 100)
        ),
        Currency(
            code = "RUB",
            name = "Rus Rublesi",
            symbol = "₽",
            country = "Rusya",
            banknoteKeywords = listOf(
                "БАНК РОССИИ", "РУБЛЬ", "РУБЛЕЙ", "Russia",
                "BANK OF RUSSIA", "RUBLE"
            ),
            denominations = listOf(50, 100, 200, 500, 1000, 2000, 5000)
        ),
        Currency(
            code = "INR",
            name = "Hint Rupisi",
            symbol = "₹",
            country = "Hindistan",
            banknoteKeywords = listOf(
                "RESERVE BANK OF INDIA", "INDIA", "RUPEE", "भारतीय रिज़र्व बैंक",
                "MAHATMA GANDHI", "रुपये", "BHARATIYA"
            ),
            denominations = listOf(10, 20, 50, 100, 200, 500, 2000)
        ),
        Currency(
            code = "BRL",
            name = "Brezilya Reali",
            symbol = "R$",
            country = "Brezilya",
            banknoteKeywords = listOf(
                "BANCO CENTRAL DO BRASIL", "REAL", "REAIS", "BRASIL", "BRAZIL"
            ),
            denominations = listOf(2, 5, 10, 20, 50, 100, 200)
        ),
        Currency(
            code = "KRW",
            name = "Güney Kore Wonu",
            symbol = "₩",
            country = "Güney Kore",
            banknoteKeywords = listOf(
                "한국은행", "BANK OF KOREA", "원", "WON", "KOREA"
            ),
            denominations = listOf(1000, 5000, 10000, 50000)
        ),
        Currency(
            code = "MXN",
            name = "Meksika Pesosu",
            symbol = "MX$",
            country = "Meksika",
            banknoteKeywords = listOf(
                "BANCO DE MEXICO", "MEXICO", "PESO", "PESOS"
            ),
            denominations = listOf(20, 50, 100, 200, 500, 1000)
        ),
        Currency(
            code = "SAR",
            name = "Suudi Riyali",
            symbol = "﷼",
            country = "Suudi Arabistan",
            banknoteKeywords = listOf(
                "SAUDI ARABIAN MONETARY", "RIYAL", "مؤسسة النقد العربي السعودي",
                "ريال", "SAUDI ARABIA", "SAMA"
            ),
            denominations = listOf(1, 5, 10, 50, 100, 500)
        ),
        Currency(
            code = "AED",
            name = "BAE Dirhemi",
            symbol = "د.إ",
            country = "Birleşik Arap Emirlikleri",
            banknoteKeywords = listOf(
                "CENTRAL BANK OF THE UAE", "DIRHAM", "UNITED ARAB EMIRATES",
                "مصرف الإمارات العربية المتحدة المركزي", "درهم"
            ),
            denominations = listOf(5, 10, 20, 50, 100, 200, 500, 1000)
        ),
        Currency(
            code = "SEK",
            name = "İsveç Kronu",
            symbol = "kr",
            country = "İsveç",
            banknoteKeywords = listOf(
                "SVERIGES RIKSBANK", "KRONA", "KRONOR", "SWEDEN", "SVERIGE"
            ),
            denominations = listOf(20, 50, 100, 200, 500, 1000)
        ),
        Currency(
            code = "NOK",
            name = "Norveç Kronu",
            symbol = "kr",
            country = "Norveç",
            banknoteKeywords = listOf(
                "NORGES BANK", "KRONE", "KRONER", "NORWAY", "NORGE"
            ),
            denominations = listOf(50, 100, 200, 500, 1000)
        ),
        Currency(
            code = "PLN",
            name = "Polonya Zlotisi",
            symbol = "zł",
            country = "Polonya",
            banknoteKeywords = listOf(
                "NARODOWY BANK POLSKI", "ZLOTY", "ZLOTYCH", "POLSKA", "POLAND"
            ),
            denominations = listOf(10, 20, 50, 100, 200, 500)
        ),
        Currency(
            code = "THB",
            name = "Tayland Bahtı",
            symbol = "฿",
            country = "Tayland",
            banknoteKeywords = listOf(
                "BANK OF THAILAND", "BAHT", "ธนาคารแห่งประเทศไทย", "บาท"
            ),
            denominations = listOf(20, 50, 100, 500, 1000)
        )
    )

    private val symbolMap: Map<String, Currency> by lazy {
        currencies.associateBy { it.symbol }
    }

    fun getAllCurrencies(): List<Currency> = currencies

    fun findByCode(code: String): Currency? =
        currencies.find { it.code.equals(code, ignoreCase = true) }

    fun findBySymbol(symbol: String): Currency? = symbolMap[symbol]

    fun matchByKeywords(text: String): List<Pair<Currency, Int>> {
        val upperText = text.uppercase()
        return currencies.map { currency ->
            val matchCount = currency.banknoteKeywords.count { keyword ->
                upperText.contains(keyword.uppercase())
            }
            currency to matchCount
        }.filter { it.second > 0 }
            .sortedByDescending { it.second }
    }

    fun findDenomination(text: String, currency: Currency): Int? {
        val numbers = Regex("\\d+").findAll(text).map { it.value.toIntOrNull() }
            .filterNotNull().toList()
        return numbers.firstOrNull { it in currency.denominations }
    }
}
