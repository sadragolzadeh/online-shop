# Online Shop – Phase 1 (Part 2)

فروشگاه آنلاین ترمینالی با جاوا (لایه‌های Model / Service / CLI / Main، ذخیره‌سازی درون حافظه).

## اجرا
- IntelliJ IDEA: پوشه را به‌عنوان پروژه Maven باز کنید و `org.example.Main` را اجرا کنید (Java 17 یا بالاتر).
- ترمینال: `mvn compile exec:java -Dexec.mainClass=org.example.Main`
  (یا بعد از `mvn package` با `java -cp target/classes org.example.Main`).

## حساب‌های نمونه
| نقش | ورود | رمز |
|---|---|---|
| مدیر | admin@shop.com | Admin@1234 |
| مشتری | ali@shop.com | Customer@1234 |

در شبیه‌ساز درگاه پرداخت، کارتِ ۱۶ رقمی که با رقم `0` تمام شود رد می‌شود (برای تست «پرداخت ناموفق»).

## مستندات
مدل دامنه، روابط، تعدّد و نگاشت داستان‌های کاربری به متدها: [`docs/domain-model.md`](docs/domain-model.md)
