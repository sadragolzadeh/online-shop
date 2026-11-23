# مدل دامنه فروشگاه آنلاین (فاز ۱ – بخش دوم)

این سند خروجی «گام اول: مدل‌سازی دامنه» است. جزئیات کامل کلاس‌ها (فیلدها با نوع داده، متدها با ورودی و خروجی) به‌صورت خودکار از روی کد جاوا استخراج شده‌اند، بنابراین دقیقاً با پیاده‌سازی یکی هستند.

علامت‌ها: `+` public ، `-` private ، `#` protected ، `{static}` عضو استاتیک.

## ۱. معماری لایه‌ای

```text
 Main  ──►  CLI (Presentation)  ──►  Service  ──►  Model
 (راه‌اندازی)   (ورودی/خروجی ترمینال)   (Use Caseها + ذخیره‌سازی در حافظه)   (Entity / Value Object / قوانین دامنه)
```

- **Model**: هیچ وابستگی به Service یا CLI ندارد. قوانین کسب‌وکار (موجودی منفی نشود، سبد فقط محصول قابل‌خرید بپذیرد، سفارش پرداخت‌شده دوباره پرداخت نشود، …) داخل خود Entity/Value Object پیاده شده‌اند.
- **Service**: هماهنگ‌کننده Use Caseها؛ هر سرویس داده‌های خودش را در `Map`/`List` درون حافظه نگه می‌دارد (لایه جدا نیست). کنترل دسترسی مدیر (`User.ensureAdmin()`) و مالکیت (سبد/سفارش/آدرس فقط برای صاحبش) اینجا اعمال می‌شود.
- **CLI**: فقط ورودی/خروجی ترمینال؛ خطاهای دامنه (`DomainException`) را می‌گیرد و پیام مناسب نشان می‌دهد.
- **Main**: ریشه ترکیب (Composition Root) و تزریق وابستگی‌ها از طریق سازنده.

## ۲. تفکیک مفاهیم دامنه

| دسته | کلاس‌ها | دلیل |
|---|---|---|
| Entity (هویت‌دار) | `User`, `Address`, `Category`, `Product`, `Cart`, `CartItem`, `Order`, `Payment`, `Review`, `ProductRating`, `StockChange` | شناسه یکتا دارند و در طول زمان وضعیتشان تغییر می‌کند |
| Value Object (تغییرناپذیر) | `Email`, `PhoneNumber`, `PostalCode`, `Money`, `Rating`, `ShippingAddress`, `OrderItem`, `PasswordResetToken`, `ProductFilter` | فقط با مقدارشان تعریف می‌شوند و در ساخت اعتبارسنجی می‌شوند |
| Enum | `UserRole`, `OrderStatus`, `PaymentStatus`, `StockChangeReason`, `ProductSortOption` | مجموعه ثابت مقادیر مجاز (مثلاً «فقط پنج گزینه مرتب‌سازی») |
| قانون/ابزار دامنه | `PasswordPolicy` (قانون رمز عبور)، `Validations` (اعتبارسنجی مشترک) | قوانین بدون وضعیت |

نکته طراحی: `OrderItem` و `ShippingAddress` «عکس لحظه‌ای» (Snapshot) هستند؛ تغییر بعدی قیمت محصول یا آدرس کاربر، سفارش‌های قبلی را تغییر نمی‌دهد (داستان ۳۵: اطلاعات سفارش‌های قبلی حذف/تغییر نکند).

## ۳. ارتباطات و تعدّد (Multiplicity)

```text
User 1 ◆────── 0..* Address              (ترکیب؛ آدرس بدون کاربر معنا ندارد)
User 1 ───────  1   UserRole
User 1 ───────  0..1 Cart                (هر کاربر حداکثر یک سبد؛ از طریق userId)
User 1 ───────  0..* Order               (از طریق userId)
User 1 ───────  0..* Review              (فقط خریداران)
User 1 ───────  0..* ProductRating       (حداکثر یکی برای هر محصول)

Cart 1 ◆────── 0..* CartItem             (ترکیب)
CartItem *─────  1   Product             (انتساب جهت‌دار CartItem → Product)

Category 1 ──── 0..* Product             (Product → Category، هر محصول دقیقاً یک دسته)
Product 1 ───── 0..* Review              (از طریق productId)
Product 1 ───── 0..* ProductRating
Product 1 ───── 0..* StockChange         (سابقه تغییر موجودی)

Order 1 ◆────── 1..* OrderItem           (Value Object، ترکیب)
Order 1 ◆────── 1   ShippingAddress      (Value Object، عکس لحظه‌ای آدرس)
Order 1 ───────  1   OrderStatus
Order 1 ───────  0..* Payment            (چند تلاش پرداخت؛ حداکثر یکی موفق)
Payment 1 ─────  1   PaymentStatus

Address 1 ◆──── 1   ShippingAddress      (جزئیات آدرس به‌صورت Value Object)
User / ShippingAddress ◇── PhoneNumber, PostalCode, Email   (Value Objectهای اعتبارسنجی‌شده)
Product / OrderItem / Cart ── Money
```

وابستگی سرویس‌ها (جهت پیکان = «استفاده می‌کند از»):

```text
AuthService      → UserService, PasswordHasher
AddressService   → UserService
ProductService   → CategoryService
InventoryService → ProductService
CartService      → ProductService
OrderService     → CartService, UserService, ProductService, InventoryService
PaymentService   → OrderService, PaymentGateway
ReviewService    → ProductService, OrderService, UserService
```

## ۴. جزئیات کامل کلاس‌ها


### Model - Entity

```text
+-------------------------------------------------------------------------------------------+
| <<Entity>> Address                                                                        |
+-------------------------------------------------------------------------------------------+
| - id: String                                                                              |
| - title: String                                                                           |
| - details: ShippingAddress                                                                |
| - defaultAddress: boolean                                                                 |
+-------------------------------------------------------------------------------------------+
| + Address(id: String, title: String, details: ShippingAddress, defaultAddress: boolean)   |
| + update(newTitle: String, newDetails: ShippingAddress): void                             |
| + markAsDefault(): void                                                                   |
| + unmarkAsDefault(): void                                                                 |
| + getId(): String                                                                         |
| + getTitle(): String                                                                      |
| + getDetails(): ShippingAddress                                                           |
| + isDefaultAddress(): boolean                                                             |
+-------------------------------------------------------------------------------------------+
```

```text
+------------------------------------------------------------+
| <<Entity>> Cart                                            |
+------------------------------------------------------------+
| - userId: String                                           |
| - items: Map<String, CartItem>                             |
+------------------------------------------------------------+
| + Cart(userId: String)                                     |
| + addProduct(product: Product, quantity: int): void        |
| + changeQuantity(productId: String, quantity: int): void   |
| + removeProduct(productId: String): void                   |
| + clear(): void                                            |
| + ensureReadyForCheckout(): void                           |
| - findItem(productId: String): CartItem                    |
| + getTotalAmount(): Money                                  |
| + getTotalQuantity(): int                                  |
| + isEmpty(): boolean                                       |
| + getUserId(): String                                      |
| + getItems(): List<CartItem>                               |
+------------------------------------------------------------+
```

```text
+-----------------------------------------------+
| <<Entity>> CartItem                           |
+-----------------------------------------------+
| - product: Product                            |
| - quantity: int                               |
+-----------------------------------------------+
| + CartItem(product: Product, quantity: int)   |
| + changeQuantity(newQuantity: int): void      |
| + increaseBy(amount: int): void               |
| - validQuantity(candidate: int): int          |
| + subtotal(): Money                           |
| + getProduct(): Product                       |
| + getQuantity(): int                          |
+-----------------------------------------------+
```

```text
+----------------------------------------+
| <<Entity>> Category                    |
+----------------------------------------+
| - id: String                           |
| - name: String                         |
| - active: boolean                      |
+----------------------------------------+
| + Category(id: String, name: String)   |
| + rename(newName: String): void        |
| + activate(): void                     |
| + deactivate(): void                   |
| + getId(): String                      |
| + getName(): String                    |
| + isActive(): boolean                  |
+----------------------------------------+
```

```text
+-------------------------------------------------------------------------------------------------+
| <<Entity>> Order                                                                                |
+-------------------------------------------------------------------------------------------------+
| - id: String                                                                                    |
| - userId: String                                                                                |
| - items: List<OrderItem>                                                                        |
| - shippingAddress: ShippingAddress                                                              |
| - totalAmount: Money                                                                            |
| - status: OrderStatus                                                                           |
| - createdAt: LocalDateTime                                                                      |
+-------------------------------------------------------------------------------------------------+
| + Order(id: String, userId: String, items: List<OrderItem>, shippingAddress: ShippingAddress)   |
| + belongsTo(candidateUserId: String): boolean                                                   |
| + ensureBelongsTo(candidateUserId: String): void                                                |
| + containsProduct(productId: String): boolean                                                   |
| + ensurePayable(): void                                                                         |
| + markAsPaid(): void                                                                            |
| + getId(): String                                                                               |
| + getUserId(): String                                                                           |
| + getItems(): List<OrderItem>                                                                   |
| + getShippingAddress(): ShippingAddress                                                         |
| + getTotalAmount(): Money                                                                       |
| + getStatus(): OrderStatus                                                                      |
| + getCreatedAt(): LocalDateTime                                                                 |
+-------------------------------------------------------------------------------------------------+
```

```text
+---------------------------------------------------------+
| <<Entity>> Payment                                      |
+---------------------------------------------------------+
| - id: String                                            |
| - orderId: String                                       |
| - amount: Money                                         |
| - status: PaymentStatus                                 |
| - transactionRef: String                                |
| - failureReason: String                                 |
| - createdAt: LocalDateTime                              |
+---------------------------------------------------------+
| + Payment(id: String, orderId: String, amount: Money)   |
| + markSuccessful(gatewayTransactionRef: String): void   |
| + markFailed(reason: String): void                      |
| - ensurePending(): void                                 |
| + getId(): String                                       |
| + getOrderId(): String                                  |
| + getAmount(): Money                                    |
| + getStatus(): PaymentStatus                            |
| + getTransactionRef(): Optional<String>                 |
| + getFailureReason(): Optional<String>                  |
| + getCreatedAt(): LocalDateTime                         |
+---------------------------------------------------------+
```

```text
+------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+
| <<Entity>> Product                                                                                                                                                           |
+------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+
| - id: String                                                                                                                                                                 |
| - name: String                                                                                                                                                               |
| - description: String                                                                                                                                                        |
| - price: Money                                                                                                                                                               |
| - stockQuantity: int                                                                                                                                                         |
| - category: Category                                                                                                                                                         |
| - brand: String                                                                                                                                                              |
| - imageUrls: List<String>                                                                                                                                                    |
| - active: boolean                                                                                                                                                            |
| - createdAt: LocalDateTime                                                                                                                                                   |
| - salesCount: int                                                                                                                                                            |
| - ratingSum: int                                                                                                                                                             |
| - ratingCount: int                                                                                                                                                           |
+------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+
| + Product(id: String, name: String, description: String, price: Money, stockQuantity: int, category: Category, brand: String, imageUrls: List<String>)                       |
| + update(newName: String, newDescription: String, newPrice: Money, newStockQuantity: int, newCategory: Category, newBrand: String, newImageUrls: List<String>): void         |
| - applyDetails(newName: String, newDescription: String, newPrice: Money, newStockQuantity: int, newCategory: Category, newBrand: String, newImageUrls: List<String>): void   |
| + activate(): void                                                                                                                                                           |
| + deactivate(): void                                                                                                                                                         |
| + isAvailable(): boolean                                                                                                                                                     |
| + ensureCanBePurchased(quantity: int): void                                                                                                                                  |
| + changeStock(delta: int): void                                                                                                                                              |
| + increaseSalesCount(quantity: int): void                                                                                                                                    |
| + addRating(rating: Rating): void                                                                                                                                            |
| + replaceRating(oldRating: Rating, newRating: Rating): void                                                                                                                  |
| + getAverageRating(): double                                                                                                                                                 |
| + matchesKeyword(keyword: String): boolean                                                                                                                                   |
| - contains(text: String, needle: String): boolean  {static}                                                                                                                  |
| + getId(): String                                                                                                                                                            |
| + getName(): String                                                                                                                                                          |
| + getDescription(): String                                                                                                                                                   |
| + getPrice(): Money                                                                                                                                                          |
| + getStockQuantity(): int                                                                                                                                                    |
| + getCategory(): Category                                                                                                                                                    |
| + getBrand(): String                                                                                                                                                         |
| + getImageUrls(): List<String>                                                                                                                                               |
| + isActive(): boolean                                                                                                                                                        |
| + getCreatedAt(): LocalDateTime                                                                                                                                              |
| + getSalesCount(): int                                                                                                                                                       |
| + getRatingCount(): int                                                                                                                                                      |
+------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+
```

```text
+----------------------------------------------------------------------+
| <<Entity>> ProductRating                                             |
+----------------------------------------------------------------------+
| - productId: String                                                  |
| - userId: String                                                     |
| - rating: Rating                                                     |
| - ratedAt: LocalDateTime                                             |
+----------------------------------------------------------------------+
| + ProductRating(productId: String, userId: String, rating: Rating)   |
| + changeTo(newRating: Rating): void                                  |
| + getProductId(): String                                             |
| + getUserId(): String                                                |
| + getRating(): Rating                                                |
| + getRatedAt(): LocalDateTime                                        |
+----------------------------------------------------------------------+
```

```text
+----------------------------------------------------------------------------------------------------+
| <<Entity>> Review                                                                                  |
+----------------------------------------------------------------------------------------------------+
| + MAX_TEXT_LENGTH: int  {static}                                                                   |
| - id: String                                                                                       |
| - productId: String                                                                                |
| - userId: String                                                                                   |
| - authorDisplayName: String                                                                        |
| - text: String                                                                                     |
| - createdAt: LocalDateTime                                                                         |
+----------------------------------------------------------------------------------------------------+
| + Review(id: String, productId: String, userId: String, authorDisplayName: String, text: String)   |
| + getId(): String                                                                                  |
| + getProductId(): String                                                                           |
| + getUserId(): String                                                                              |
| + getAuthorDisplayName(): String                                                                   |
| + getText(): String                                                                                |
| + getCreatedAt(): LocalDateTime                                                                    |
+----------------------------------------------------------------------------------------------------+
```

```text
+---------------------------------------------------------------------------------------------------------------------------------------+
| <<Entity>> StockChange                                                                                                                |
+---------------------------------------------------------------------------------------------------------------------------------------+
| - id: String                                                                                                                          |
| - productId: String                                                                                                                   |
| - previousQuantity: int                                                                                                               |
| - newQuantity: int                                                                                                                    |
| - reason: StockChangeReason                                                                                                           |
| - changedBy: String                                                                                                                   |
| - changedAt: LocalDateTime                                                                                                            |
+---------------------------------------------------------------------------------------------------------------------------------------+
| + StockChange(id: String, productId: String, previousQuantity: int, newQuantity: int, reason: StockChangeReason, changedBy: String)   |
| + getDelta(): int                                                                                                                     |
| + getId(): String                                                                                                                     |
| + getProductId(): String                                                                                                              |
| + getPreviousQuantity(): int                                                                                                          |
| + getNewQuantity(): int                                                                                                               |
| + getReason(): StockChangeReason                                                                                                      |
| + getChangedBy(): String                                                                                                              |
| + getChangedAt(): LocalDateTime                                                                                                       |
+---------------------------------------------------------------------------------------------------------------------------------------+
```

```text
+----------------------------------------------------------------------------------------------------------------+
| <<Entity>> User                                                                                                |
+----------------------------------------------------------------------------------------------------------------+
| - id: String                                                                                                   |
| - fullName: String                                                                                             |
| - email: Email                                                                                                 |
| - phone: PhoneNumber                                                                                           |
| - passwordHash: String                                                                                         |
| - role: UserRole                                                                                               |
| - active: boolean                                                                                              |
| - createdAt: LocalDateTime                                                                                     |
| - addresses: List<Address>                                                                                     |
+----------------------------------------------------------------------------------------------------------------+
| + User(id: String, fullName: String, email: Email, phone: PhoneNumber, passwordHash: String, role: UserRole)   |
| + updateProfile(newFullName: String, newEmail: Email, newPhone: PhoneNumber): void                             |
| + changePasswordHash(newPasswordHash: String): void                                                            |
| + activate(): void                                                                                             |
| + deactivate(): void                                                                                           |
| + isAdmin(): boolean                                                                                           |
| + ensureAdmin(): void                                                                                          |
| + hasEmail(candidate: Email): boolean                                                                          |
| + hasPhone(candidate: PhoneNumber): boolean                                                                    |
| + addAddress(address: Address): void                                                                           |
| + getAddress(addressId: String): Address                                                                       |
| + removeAddress(addressId: String): void                                                                       |
| + setDefaultAddress(addressId: String): void                                                                   |
| - clearDefaultAddress(): void                                                                                  |
| - requireAtLeastOneContact(email: Email, phone: PhoneNumber): void  {static}                                   |
| + getId(): String                                                                                              |
| + getFullName(): String                                                                                        |
| + getEmail(): Optional<Email>                                                                                  |
| + getPhone(): Optional<PhoneNumber>                                                                            |
| + getPasswordHash(): String                                                                                    |
| + getRole(): UserRole                                                                                          |
| + isActive(): boolean                                                                                          |
| + getCreatedAt(): LocalDateTime                                                                                |
| + getAddresses(): List<Address>                                                                                |
+----------------------------------------------------------------------------------------------------------------+
```


### Model - Value Object

```text
+------------------------------------+
| <<Value Object>> Email  (record)   |
+------------------------------------+
| + value: String                    |
+------------------------------------+
| - FORMAT: Pattern  {static}        |
+------------------------------------+
| + toString(): String               |
+------------------------------------+
```

```text
+--------------------------------------------------------------------+
| <<Value Object>> Money  (record)  [implements Comparable<Money>]   |
+--------------------------------------------------------------------+
| + amount: BigDecimal                                               |
+--------------------------------------------------------------------+
| + ZERO: Money  {static}                                            |
+--------------------------------------------------------------------+
| + of(value: long): Money  {static}                                 |
| + add(other: Money): Money                                         |
| + multiply(factor: int): Money                                     |
| + isZero(): boolean                                                |
| + compareTo(other: Money): int                                     |
| + toString(): String                                               |
+--------------------------------------------------------------------+
```

```text
+----------------------------------------+
| <<Value Object>> OrderItem  (record)   |
+----------------------------------------+
| + productId: String                    |
| + productName: String                  |
| + unitPrice: Money                     |
| + quantity: int                        |
+----------------------------------------+
| + totalPrice(): Money                  |
+----------------------------------------+
```

```text
+---------------------------------------------------+
| <<Value Object>> PasswordPolicy                   |
+---------------------------------------------------+
| + MIN_LENGTH: int  {static}                       |
+---------------------------------------------------+
| - PasswordPolicy()                                |
| + validate(rawPassword: String): void  {static}   |
+---------------------------------------------------+
```

```text
+-------------------------------------------------+
| <<Value Object>> PasswordResetToken  (record)   |
+-------------------------------------------------+
| + userId: String                                |
| + code: String                                  |
| + expiresAt: LocalDateTime                      |
+-------------------------------------------------+
| + isExpired(now: LocalDateTime): boolean        |
| + matches(candidateCode: String): boolean       |
+-------------------------------------------------+
```

```text
+----------------------------------------------+
| <<Value Object>> PhoneNumber  (record)       |
+----------------------------------------------+
| + value: String                              |
+----------------------------------------------+
| - FORMAT: Pattern  {static}                  |
+----------------------------------------------+
| - normalise(raw: String): String  {static}   |
| + toString(): String                         |
+----------------------------------------------+
```

```text
+-----------------------------------------+
| <<Value Object>> PostalCode  (record)   |
+-----------------------------------------+
| + value: String                         |
+-----------------------------------------+
| - FORMAT: Pattern  {static}             |
+-----------------------------------------+
| + toString(): String                    |
+-----------------------------------------+
```

```text
+------------------------------------------------+
| <<Value Object>> ProductFilter                 |
+------------------------------------------------+
| - categoryId: String                           |
| - minPrice: Money                              |
| - maxPrice: Money                              |
| - brand: String                                |
| - minRating: Rating                            |
| - inStockOnly: boolean                         |
+------------------------------------------------+
| - ProductFilter(builder: Builder)              |
| + builder(): Builder  {static}                 |
| + getCategoryId(): Optional<String>            |
| + matches(product: Product): boolean           |
| - matchesCategory(product: Product): boolean   |
| - matchesPrice(product: Product): boolean      |
| - matchesBrand(product: Product): boolean      |
| - matchesRating(product: Product): boolean     |
+------------------------------------------------+
```

```text
+-------------------------------------+
| <<Value Object>> Rating  (record)   |
+-------------------------------------+
| + value: int                        |
+-------------------------------------+
| + MIN: int  {static}                |
| + MAX: int  {static}                |
+-------------------------------------+
```

```text
+----------------------------------------------+
| <<Value Object>> ShippingAddress  (record)   |
+----------------------------------------------+
| + recipientName: String                      |
| + phone: PhoneNumber                         |
| + country: String                            |
| + city: String                               |
| + street: String                             |
| + postalCode: PostalCode                     |
+----------------------------------------------+
| + asSingleLine(): String                     |
+----------------------------------------------+
```


### Model - Enum

```text
+--------------------------------------------------------------------------------+
| <<Enum>> OrderStatus                                                           |
+--------------------------------------------------------------------------------+
| constants: AWAITING_PAYMENT, PAID, PROCESSING, SHIPPED, DELIVERED, CANCELLED   |
+--------------------------------------------------------------------------------+
| - label: String                                                                |
| - paidState: boolean                                                           |
+--------------------------------------------------------------------------------+
| + getLabel(): String                                                           |
| + isPaidState(): boolean                                                       |
+--------------------------------------------------------------------------------+
```

```text
+------------------------------------------+
| <<Enum>> PaymentStatus                   |
+------------------------------------------+
| constants: PENDING, SUCCESSFUL, FAILED   |
+------------------------------------------+
| - label: String                          |
+------------------------------------------+
| + getLabel(): String                     |
+------------------------------------------+
```

```text
+----------------------------------------------------------------------------+
| <<Enum>> ProductSortOption                                                 |
+----------------------------------------------------------------------------+
| constants: CHEAPEST, MOST_EXPENSIVE, NEWEST, MOST_POPULAR, HIGHEST_RATED   |
+----------------------------------------------------------------------------+
| - label: String                                                            |
| - comparator: Comparator<Product>                                          |
+----------------------------------------------------------------------------+
| + getLabel(): String                                                       |
| + getComparator(): Comparator<Product>                                     |
+----------------------------------------------------------------------------+
```

```text
+--------------------------------------------+
| <<Enum>> StockChangeReason                 |
+--------------------------------------------+
| constants: MANUAL_ADJUSTMENT, ORDER_PAID   |
+--------------------------------------------+
| - label: String                            |
+--------------------------------------------+
| + getLabel(): String                       |
+--------------------------------------------+
```

```text
+-----------------------+
| <<Enum>> UserRole     |
+-----------------------+
| constants: CUSTOMER   |
+-----------------------+
```


### Model - Exception

```text
+------------------------------------------------------------------+
| <<Exception>> AccessDeniedException  [extends DomainException]   |
+------------------------------------------------------------------+
| + AccessDeniedException(message: String)                         |
+------------------------------------------------------------------+
```

```text
+------------------------------------------------------------------+
| <<Exception>> BusinessRuleException  [extends DomainException]   |
+------------------------------------------------------------------+
| + BusinessRuleException(message: String)                         |
+------------------------------------------------------------------+
```

```text
+-------------------------------------------------------------+
| <<Exception>> DomainException  [extends RuntimeException]   |
+-------------------------------------------------------------+
| + DomainException(message: String)                          |
+-------------------------------------------------------------+
```

```text
+--------------------------------------------------------------+
| <<Exception>> NotFoundException  [extends DomainException]   |
+--------------------------------------------------------------+
| + NotFoundException(message: String)                         |
+--------------------------------------------------------------+
```

```text
+----------------------------------------------------------------+
| <<Exception>> ValidationException  [extends DomainException]   |
+----------------------------------------------------------------+
| + ValidationException(message: String)                         |
+----------------------------------------------------------------+
```


### Model - Validation

```text
+-------------------------------------------------------------------------------------------+
| <<Utility>> Validations                                                                   |
+-------------------------------------------------------------------------------------------+
| - Validations()                                                                           |
| + requireNotBlank(value: String, fieldLabel: String): String  {static}                    |
| + requireMaxLength(value: String, maxLength: int, fieldLabel: String): String  {static}   |
| + requireNonNegative(value: int, fieldLabel: String): int  {static}                       |
| + requirePositive(value: int, fieldLabel: String): int  {static}                          |
+-------------------------------------------------------------------------------------------+
```


### Service

```text
+----------------------------------------------------------------------------------------+
| <<Service>> AddressService                                                             |
+----------------------------------------------------------------------------------------+
| - userService: UserService                                                             |
| - idGenerator: IdGenerator                                                             |
+----------------------------------------------------------------------------------------+
| + AddressService(userService: UserService)                                             |
| + getAddresses(userId: String): List<Address>                                          |
| + addAddress(userId: String, request: AddressRequest): Address                         |
| + updateAddress(userId: String, addressId: String, request: AddressRequest): Address   |
| + deleteAddress(userId: String, addressId: String): void                               |
| + setDefaultAddress(userId: String, addressId: String): void                           |
+----------------------------------------------------------------------------------------+
```

```text
+--------------------------------------------------------------------------------------+
| <<Service>> AuthService                                                              |
+--------------------------------------------------------------------------------------+
| - RESET_CODE_VALIDITY_MINUTES: int  {static}                                         |
| - RESET_CODE_BOUND: int  {static}                                                    |
| - userService: UserService                                                           |
| - passwordHasher: PasswordHasher                                                     |
| - resetTokens: Map<String, PasswordResetToken>                                       |
| - random: SecureRandom                                                               |
| - currentUser: User                                                                  |
+--------------------------------------------------------------------------------------+
| + AuthService(userService: UserService, passwordHasher: PasswordHasher)              |
| + register(fullName: String, email: String, phone: String, password: String): User   |
| + login(identifier: String, password: String): User                                  |
| + logout(): void                                                                     |
| + getCurrentUser(): Optional<User>                                                   |
| + requireCurrentUser(): User                                                         |
| + requestPasswordReset(identifier: String): String                                   |
| + resetPassword(identifier: String, code: String, newPassword: String): void         |
| - findRegisteredUser(identifier: String): User                                       |
+--------------------------------------------------------------------------------------+
```

```text
+----------------------------------------------------------------------------+
| <<Service>> CartService                                                    |
+----------------------------------------------------------------------------+
| - carts: Map<String, Cart>                                                 |
| - productService: ProductService                                           |
+----------------------------------------------------------------------------+
| + CartService(productService: ProductService)                              |
| + getCart(userId: String): Cart                                            |
| + addProduct(userId: String, productId: String, quantity: int): Cart       |
| + changeQuantity(userId: String, productId: String, quantity: int): Cart   |
| + removeProduct(userId: String, productId: String): Cart                   |
| + clear(userId: String): Cart                                              |
+----------------------------------------------------------------------------+
```

```text
+---------------------------------------------------------+
| <<Service>> CategoryService                             |
+---------------------------------------------------------+
| - categories: Map<String, Category>                     |
| - idGenerator: IdGenerator                              |
+---------------------------------------------------------+
| + createCategory(admin: User, name: String): Category   |
| + getActiveCategories(): List<Category>                 |
| + getActiveCategory(categoryId: String): Category       |
+---------------------------------------------------------+
```

```text
+---------------------------------+
| <<Service>> IdGenerator         |
+---------------------------------+
| - prefix: String                |
| - counter: AtomicLong           |
+---------------------------------+
| + IdGenerator(prefix: String)   |
| + next(): String                |
+---------------------------------+
```

```text
+-------------------------------------------------------------------------------------------------------+
| <<Service>> InventoryService                                                                          |
+-------------------------------------------------------------------------------------------------------+
| - SYSTEM_ACTOR: String  {static}                                                                      |
| - productService: ProductService                                                                      |
| - history: List<StockChange>                                                                          |
| - idGenerator: IdGenerator                                                                            |
+-------------------------------------------------------------------------------------------------------+
| + InventoryService(productService: ProductService)                                                    |
| + getInventory(admin: User): List<Product>                                                            |
| + adjustStock(admin: User, productId: String, delta: int): StockChange                                |
| + recordSale(productId: String, quantity: int): StockChange                                           |
| + getHistory(admin: User, productId: String): List<StockChange>                                       |
| - applyChange(productId: String, delta: int, reason: StockChangeReason, actor: String): StockChange   |
+-------------------------------------------------------------------------------------------------------+
```

```text
+------------------------------------------------------------------------------------------------------------------------------------------+
| <<Service>> OrderService                                                                                                                 |
+------------------------------------------------------------------------------------------------------------------------------------------+
| - orders: Map<String, Order>                                                                                                             |
| - idGenerator: IdGenerator                                                                                                               |
| - cartService: CartService                                                                                                               |
| - userService: UserService                                                                                                               |
| - productService: ProductService                                                                                                         |
| - inventoryService: InventoryService                                                                                                     |
+------------------------------------------------------------------------------------------------------------------------------------------+
| + OrderService(cartService: CartService, userService: UserService, productService: ProductService, inventoryService: InventoryService)   |
| + startCheckout(userId: String): Cart                                                                                                    |
| + placeOrder(userId: String, addressId: String): Order                                                                                   |
| + getOrdersOfUser(userId: String): List<Order>                                                                                           |
| + getOrderOfUser(userId: String, orderId: String): Order                                                                                 |
| + hasUserPurchased(userId: String, productId: String): boolean                                                                           |
| + getAllOrders(admin: User, statusFilter: OrderStatus): List<Order>                                                                      |
| + getOrderForAdmin(admin: User, orderId: String): Order                                                                                  |
| + ensureOrderItemsPurchasable(order: Order): void                                                                                        |
| + completePayment(order: Order): void                                                                                                    |
| - findOrder(orderId: String): Order                                                                                                      |
| - toOrderItem(cartItem: CartItem): OrderItem  {static}                                                                                   |
+------------------------------------------------------------------------------------------------------------------------------------------+
```

```text
+---------------------------------------------------------------+
| <<Service>> PasswordHasher                                    |
+---------------------------------------------------------------+
| + hash(rawPassword: String): String                           |
| + matches(rawPassword: String, storedHash: String): boolean   |
+---------------------------------------------------------------+
```

```text
+---------------------------------------------------------------------------------+
| <<Service>> PaymentService                                                      |
+---------------------------------------------------------------------------------+
| - CARD_NUMBER_FORMAT: String  {static}                                          |
| - orderService: OrderService                                                    |
| - paymentGateway: PaymentGateway                                                |
| - payments: List<Payment>                                                       |
| - acceptedTransactionRefs: Set<String>                                          |
| - idGenerator: IdGenerator                                                      |
+---------------------------------------------------------------------------------+
| + PaymentService(orderService: OrderService, paymentGateway: PaymentGateway)    |
| + payOrder(userId: String, orderId: String, cardNumber: String): Payment        |
| + getPaymentsOfOrder(userId: String, orderId: String): List<Payment>            |
| - verifyAndApply(order: Order, payment: Payment, result: GatewayResult): void   |
| - validateCardNumber(cardNumber: String): void  {static}                        |
+---------------------------------------------------------------------------------+
```

```text
+-----------------------------------------------------------------+
| <<Service>> Pbkdf2PasswordHasher  [implements PasswordHasher]   |
+-----------------------------------------------------------------+
| - ALGORITHM: String  {static}                                   |
| - ITERATIONS: int  {static}                                     |
| - KEY_LENGTH_BITS: int  {static}                                |
| - SALT_LENGTH_BYTES: int  {static}                              |
| - random: SecureRandom                                          |
+-----------------------------------------------------------------+
| + hash(rawPassword: String): String                             |
| + matches(rawPassword: String, storedHash: String): boolean     |
| - derive(rawPassword: String, salt: byte[]): byte[]             |
+-----------------------------------------------------------------+
```

```text
+-------------------------------------------------------------------------------------+
| <<Service>> ProductService                                                          |
+-------------------------------------------------------------------------------------+
| - MAX_SEARCH_LENGTH: int  {static}                                                  |
| - products: Map<String, Product>                                                    |
| - idGenerator: IdGenerator                                                          |
| - categoryService: CategoryService                                                  |
+-------------------------------------------------------------------------------------+
| + ProductService(categoryService: CategoryService)                                  |
| + createProduct(admin: User, request: ProductRequest): Product                      |
| + updateProduct(admin: User, productId: String, request: ProductRequest): Product   |
| + deactivateProduct(admin: User, productId: String): Product                        |
| + getAllProducts(admin: User): List<Product>                                        |
| + getProduct(productId: String): Product                                            |
| + getActiveProducts(): List<Product>                                                |
| + getProductsByCategory(categoryId: String): List<Product>                          |
| + searchProducts(query: String): List<Product>                                      |
| + filterProducts(filter: ProductFilter): List<Product>                              |
| + sortProducts(source: List<Product>, option: ProductSortOption): List<Product>     |
| - activeProducts(): Stream<Product>                                                 |
+-------------------------------------------------------------------------------------+
```

```text
+---------------------------------------------------------------------------------------------------------+
| <<Service>> ReviewService                                                                               |
+---------------------------------------------------------------------------------------------------------+
| - productService: ProductService                                                                        |
| - orderService: OrderService                                                                            |
| - userService: UserService                                                                              |
| - reviews: List<Review>                                                                                 |
| - ratings: Map<String, ProductRating>                                                                   |
| - idGenerator: IdGenerator                                                                              |
+---------------------------------------------------------------------------------------------------------+
| + ReviewService(productService: ProductService, orderService: OrderService, userService: UserService)   |
| + addReview(userId: String, productId: String, text: String): Review                                    |
| + rateProduct(userId: String, productId: String, score: int): void                                      |
| + getReviews(productId: String): List<Review>                                                           |
| - ensurePurchased(userId: String, productId: String): void                                              |
| - firstNameOf(user: User): String  {static}                                                             |
+---------------------------------------------------------------------------------------------------------+
```

```text
+--------------------------------------------------------------------------------------------------------------------------------------------------+
| <<Service>> SampleDataLoader                                                                                                                     |
+--------------------------------------------------------------------------------------------------------------------------------------------------+
| + ADMIN_EMAIL: String  {static}                                                                                                                  |
| + ADMIN_PASSWORD: String  {static}                                                                                                               |
| + CUSTOMER_EMAIL: String  {static}                                                                                                               |
| + CUSTOMER_PASSWORD: String  {static}                                                                                                            |
| - userService: UserService                                                                                                                       |
| - passwordHasher: PasswordHasher                                                                                                                 |
| - categoryService: CategoryService                                                                                                               |
| - productService: ProductService                                                                                                                 |
+--------------------------------------------------------------------------------------------------------------------------------------------------+
| + SampleDataLoader(userService: UserService, passwordHasher: PasswordHasher, categoryService: CategoryService, productService: ProductService)   |
| + load(): void                                                                                                                                   |
| - addProduct(admin: User, name: String, description: String, price: long, stock: int, category: Category, brand: String): void                   |
+--------------------------------------------------------------------------------------------------------------------------------------------------+
```

```text
+------------------------------------------------------------------------------------------------------------+
| <<Service>> UserService                                                                                    |
+------------------------------------------------------------------------------------------------------------+
| - users: Map<String, User>                                                                                 |
| - idGenerator: IdGenerator                                                                                 |
+------------------------------------------------------------------------------------------------------------+
| + createUser(fullName: String, email: String, phone: String, passwordHash: String, role: UserRole): User   |
| + getUser(userId: String): User                                                                            |
| + findByIdentifier(identifier: String): Optional<User>                                                     |
| + updateProfile(userId: String, fullName: String, email: String, phone: String): User                      |
| + changePasswordHash(userId: String, newPasswordHash: String): void                                        |
| - ensureContactIsUnique(email: Email, phone: PhoneNumber, excludedUserId: String): void                    |
| - parseEmail(raw: String): Email  {static}                                                                 |
| - parsePhone(raw: String): PhoneNumber  {static}                                                           |
+------------------------------------------------------------------------------------------------------------+
```


### Service - DTO

```text
+------------------------------------------+
| <<DTO>> AddressRequest  (record)         |
+------------------------------------------+
| + title: String                          |
| + recipientName: String                  |
| + phone: String                          |
| + country: String                        |
| + city: String                           |
| + street: String                         |
| + postalCode: String                     |
| + makeDefault: boolean                   |
+------------------------------------------+
| + toShippingAddress(): ShippingAddress   |
+------------------------------------------+
```

```text
+------------------------------------+
| <<DTO>> ProductRequest  (record)   |
+------------------------------------+
| + name: String                     |
| + description: String              |
| + price: BigDecimal                |
| + stockQuantity: int               |
| + categoryId: String               |
| + brand: String                    |
| + imageUrls: List<String>          |
+------------------------------------+
```


### Service - Payment Gateway

```text
+----------------------------------------------+
| <<Port / Adapter>> GatewayResult  (record)   |
+----------------------------------------------+
| + approved: boolean                          |
| + transactionRef: String                     |
| + paidAmount: Money                          |
| + message: String                            |
+----------------------------------------------+
```

```text
+-------------------------------------------------------------------------------+
| <<Port / Adapter>> PaymentGateway                                             |
+-------------------------------------------------------------------------------+
| + charge(orderId: String, amount: Money, cardNumber: String): GatewayResult   |
+-------------------------------------------------------------------------------+
```

```text
+-------------------------------------------------------------------------------+
| <<Port / Adapter>> SimulatedPaymentGateway  [implements PaymentGateway]       |
+-------------------------------------------------------------------------------+
| + charge(orderId: String, amount: Money, cardNumber: String): GatewayResult   |
+-------------------------------------------------------------------------------+
```


### CLI (Presentation)

```text
+------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+
| <<CLI>> AdminMenu  [extends Menu]                                                                                                                                                                                |
+------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+
| - printer: EntityPrinter                                                                                                                                                                                         |
| - authService: AuthService                                                                                                                                                                                       |
| - productService: ProductService                                                                                                                                                                                 |
| - categoryService: CategoryService                                                                                                                                                                               |
| - inventoryService: InventoryService                                                                                                                                                                             |
| - orderService: OrderService                                                                                                                                                                                     |
+------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+
| + AdminMenu(io: ConsoleIO, printer: EntityPrinter, authService: AuthService, productService: ProductService, categoryService: CategoryService, inventoryService: InventoryService, orderService: OrderService)   |
| + open(): void                                                                                                                                                                                                   |
| - admin(): User                                                                                                                                                                                                  |
| - createCategory(): void                                                                                                                                                                                         |
| - createProduct(): void                                                                                                                                                                                          |
| - editProduct(): void                                                                                                                                                                                            |
| - deactivateProduct(): void                                                                                                                                                                                      |
| - showInventory(): void                                                                                                                                                                                          |
| - adjustStock(): void                                                                                                                                                                                            |
| - showStockHistory(): void                                                                                                                                                                                       |
| - listOrders(): void                                                                                                                                                                                             |
| - showOrderDetails(): void                                                                                                                                                                                       |
| - readProductRequest(current: Product): ProductRequest                                                                                                                                                           |
| - askText(label: String, current: String): String                                                                                                                                                                |
| - askDecimal(label: String, current: BigDecimal): BigDecimal                                                                                                                                                     |
| - askInt(label: String, current: Integer): int                                                                                                                                                                   |
| - splitImages(raw: String): List<String>  {static}                                                                                                                                                               |
+------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+
```

```text
+-------------------------------------------------------+
| <<CLI>> AuthMenu  [extends Menu]                      |
+-------------------------------------------------------+
| - authService: AuthService                            |
+-------------------------------------------------------+
| + AuthMenu(io: ConsoleIO, authService: AuthService)   |
| + open(): void                                        |
| - register(): void                                    |
| - login(): void                                       |
| - recoverPassword(): void                             |
+-------------------------------------------------------+
```

```text
+---------------------------------------------------------------------------------------------------------+
| <<CLI>> CartMenu  [extends Menu]                                                                        |
+---------------------------------------------------------------------------------------------------------+
| - printer: EntityPrinter                                                                                |
| - authService: AuthService                                                                              |
| - cartService: CartService                                                                              |
+---------------------------------------------------------------------------------------------------------+
| + CartMenu(io: ConsoleIO, printer: EntityPrinter, authService: AuthService, cartService: CartService)   |
| + open(): void                                                                                          |
| - userId(): String                                                                                      |
| - showCart(): void                                                                                      |
| - addProduct(): void                                                                                    |
| - changeQuantity(): void                                                                                |
| - removeProduct(): void                                                                                 |
| - clearCart(): void                                                                                     |
+---------------------------------------------------------------------------------------------------------+
```

```text
+------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+
| <<CLI>> CatalogMenu  [extends Menu]                                                                                                                                                                        |
+------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+
| - NO_PRODUCTS: String  {static}                                                                                                                                                                            |
| - printer: EntityPrinter                                                                                                                                                                                   |
| - authService: AuthService                                                                                                                                                                                 |
| - productService: ProductService                                                                                                                                                                           |
| - categoryService: CategoryService                                                                                                                                                                         |
| - reviewService: ReviewService                                                                                                                                                                             |
| - cartService: CartService                                                                                                                                                                                 |
+------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+
| + CatalogMenu(io: ConsoleIO, printer: EntityPrinter, authService: AuthService, productService: ProductService, categoryService: CategoryService, reviewService: ReviewService, cartService: CartService)   |
| + open(): void                                                                                                                                                                                             |
| - listProducts(): void                                                                                                                                                                                     |
| - listCategories(): void                                                                                                                                                                                   |
| - listProductsOfCategory(): void                                                                                                                                                                           |
| - showProductDetails(): void                                                                                                                                                                               |
| - search(): void                                                                                                                                                                                           |
| - filter(): void                                                                                                                                                                                           |
| - sort(): void                                                                                                                                                                                             |
| - addToCart(): void                                                                                                                                                                                        |
| - addReview(): void                                                                                                                                                                                        |
| - rateProduct(): void                                                                                                                                                                                      |
+------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+
```

```text
+-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+
| <<CLI>> CliApplication  [extends Menu]                                                                                                                                                              |
+-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+
| - authService: AuthService                                                                                                                                                                          |
| - authMenu: AuthMenu                                                                                                                                                                                |
| - catalogMenu: CatalogMenu                                                                                                                                                                          |
| - profileMenu: ProfileMenu                                                                                                                                                                          |
| - cartMenu: CartMenu                                                                                                                                                                                |
| - orderMenu: OrderMenu                                                                                                                                                                              |
| - adminMenu: AdminMenu                                                                                                                                                                              |
+-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+
| + CliApplication(io: ConsoleIO, authService: AuthService, authMenu: AuthMenu, catalogMenu: CatalogMenu, profileMenu: ProfileMenu, cartMenu: CartMenu, orderMenu: OrderMenu, adminMenu: AdminMenu)   |
| + open(): void                                                                                                                                                                                      |
| - currentItems(): List<MenuItem>                                                                                                                                                                    |
| - guestItems(): List<MenuItem>                                                                                                                                                                      |
| - customerItems(): List<MenuItem>                                                                                                                                                                   |
| - adminItems(): List<MenuItem>                                                                                                                                                                      |
| - logout(): void                                                                                                                                                                                    |
+-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+
```

```text
+----------------------------------------------------------------------+
| <<CLI>> ConsoleIO                                                    |
+----------------------------------------------------------------------+
| - YES_ANSWERS: Set<String>  {static}                                 |
| - scanner: Scanner                                                   |
| - out: PrintStream                                                   |
+----------------------------------------------------------------------+
| + ConsoleIO(in: InputStream, out: PrintStream)                       |
| + println(): void                                                    |
| + println(text: String): void                                        |
| + printError(message: String): void                                  |
| + printSuccess(message: String): void                                |
| + readLine(prompt: String): String                                   |
| + readInt(prompt: String): int                                       |
| + readDecimal(prompt: String): BigDecimal                            |
| + readRequired(prompt: String, parser: Function<String, T>): <T> T   |
| + readOptional(prompt: String, parser: Function<String, T>): <T> T   |
| + confirm(prompt: String): boolean                                   |
+----------------------------------------------------------------------+
```

```text
+------------------------------------------------------------------------+
| <<CLI>> EntityPrinter                                                  |
+------------------------------------------------------------------------+
| - DATE_FORMAT: DateTimeFormatter  {static}                             |
| - io: ConsoleIO                                                        |
+------------------------------------------------------------------------+
| + EntityPrinter(io: ConsoleIO)                                         |
| + printCategories(categories: List<Category>): void                    |
| + printProducts(products: List<Product>, emptyMessage: String): void   |
| + printProductDetails(product: Product, reviews: List<Review>): void   |
| + printCart(cart: Cart): void                                          |
| + printAddresses(addresses: List<Address>): void                       |
| + printProfile(user: User): void                                       |
| + printOrders(orders: List<Order>): void                               |
| + printOrderDetails(order: Order): void                                |
| + printPayments(payments: List<Payment>): void                         |
| + printInventory(products: List<Product>): void                        |
| + printStockChanges(changes: List<StockChange>): void                  |
| - formatRating(product: Product): String  {static}                     |
| - formatDate(dateTime: LocalDateTime): String  {static}                |
+------------------------------------------------------------------------+
```

```text
+------------------------------------------------------------+
| <<CLI>> InputClosedException  [extends RuntimeException]   |
+------------------------------------------------------------+
| + InputClosedException()                                   |
+------------------------------------------------------------+
```

```text
+--------------------------------------------------------------------------------+
| <<CLI>> Menu                                                                   |
+--------------------------------------------------------------------------------+
| # io: ConsoleIO                                                                |
+--------------------------------------------------------------------------------+
| # Menu(io: ConsoleIO)                                                          |
| # showOnce(title: String, items: List<MenuItem>, exitLabel: String): boolean   |
| # loop(title: String, items: List<MenuItem>): void                             |
| # runSafely(action: Runnable): void                                            |
+--------------------------------------------------------------------------------+
```

```text
+------------------------------+
| <<CLI>> MenuItem  (record)   |
+------------------------------+
| + label: String              |
| + action: Runnable           |
+------------------------------+
```

```text
+----------------------------------------------------------------------------------------------------------------------------------------------------------------------------+
| <<CLI>> OrderMenu  [extends Menu]                                                                                                                                          |
+----------------------------------------------------------------------------------------------------------------------------------------------------------------------------+
| - printer: EntityPrinter                                                                                                                                                   |
| - authService: AuthService                                                                                                                                                 |
| - addressService: AddressService                                                                                                                                           |
| - orderService: OrderService                                                                                                                                               |
| - paymentService: PaymentService                                                                                                                                           |
+----------------------------------------------------------------------------------------------------------------------------------------------------------------------------+
| + OrderMenu(io: ConsoleIO, printer: EntityPrinter, authService: AuthService, addressService: AddressService, orderService: OrderService, paymentService: PaymentService)   |
| + open(): void                                                                                                                                                             |
| - userId(): String                                                                                                                                                         |
| - checkout(): void                                                                                                                                                         |
| - chooseAddressId(addresses: List<Address>): String                                                                                                                        |
| - payExistingOrder(): void                                                                                                                                                 |
| - pay(orderId: String): void                                                                                                                                               |
| - listOrders(): void                                                                                                                                                       |
| - showOrderDetails(): void                                                                                                                                                 |
+----------------------------------------------------------------------------------------------------------------------------------------------------------------------------+
```

```text
+--------------------------------------------------------------------------------------------------------------------------------------------+
| <<CLI>> ProfileMenu  [extends Menu]                                                                                                        |
+--------------------------------------------------------------------------------------------------------------------------------------------+
| - KEEP_HINT: String  {static}                                                                                                              |
| - printer: EntityPrinter                                                                                                                   |
| - authService: AuthService                                                                                                                 |
| - userService: UserService                                                                                                                 |
| - addressService: AddressService                                                                                                           |
+--------------------------------------------------------------------------------------------------------------------------------------------+
| + ProfileMenu(io: ConsoleIO, printer: EntityPrinter, authService: AuthService, userService: UserService, addressService: AddressService)   |
| + open(): void                                                                                                                             |
| - showProfile(): void                                                                                                                      |
| - editProfile(): void                                                                                                                      |
| - askKeep(label: String, current: String): String                                                                                          |
| - openAddressMenu(): void                                                                                                                  |
| - listAddresses(): void                                                                                                                    |
| - addAddress(): void                                                                                                                       |
| - editAddress(): void                                                                                                                      |
| - deleteAddress(): void                                                                                                                    |
| - setDefaultAddress(): void                                                                                                                |
| - readAddressRequest(): AddressRequest                                                                                                     |
+--------------------------------------------------------------------------------------------------------------------------------------------+
```


### Main

```text
+------------------------------------------+
| <<Main>> Main                            |
+------------------------------------------+
| - Main()                                 |
| + main(args: String[]): void  {static}   |
+------------------------------------------+
```


## ۵. نگاشت داستان‌های کاربری به فعالیت‌ها (متدها)

| داستان | فعالیت | متد(های) اصلی |
|---|---|---|
| ۱ ثبت‌نام | ایجاد حساب با ایمیل/موبایل یکتا و رمز رمزنگاری‌شده | `AuthService.register` ← `UserService.createUser`, `PasswordHasher.hash`, `PasswordPolicy.validate` |
| ۲ ورود | بررسی اطلاعات و ساخت نشست | `AuthService.login` |
| ۳ خروج | پاک کردن نشست | `AuthService.logout`, `AuthService.requireCurrentUser` |
| ۴ فراموشی رمز | صدور کد و تعیین رمز جدید | `AuthService.requestPasswordReset`, `AuthService.resetPassword` |
| ۵ مشاهده پروفایل | نمایش بدون رمز | `ProfileMenu.showProfile`, `EntityPrinter.printProfile` |
| ۶ ویرایش پروفایل | ویرایش با اعتبارسنجی و یکتایی | `UserService.updateProfile`, `User.updateProfile` |
| ۷ مدیریت آدرس | افزودن/ویرایش/حذف/پیش‌فرض | `AddressService.*`, `User.addAddress/removeAddress/setDefaultAddress` |
| ۸ مشاهده محصولات | فقط محصولات فعال | `ProductService.getActiveProducts` |
| ۹ جزئیات محصول | محصول + نظرات | `ProductService.getProduct`, `ReviewService.getReviews` |
| ۱۰ جستجو | حداکثر ۱۰۰ کاراکتر، فقط فعال | `ProductService.searchProducts`, `Product.matchesKeyword` |
| ۱۱ فیلتر | دسته، قیمت، برند، امتیاز، موجودی | `ProductService.filterProducts`, `ProductFilter.matches` |
| ۱۲ مرتب‌سازی | فقط پنج گزینه مجاز | `ProductService.sortProducts`, `ProductSortOption` |
| ۱۳ دسته‌بندی‌ها | فقط فعال | `CategoryService.getActiveCategories` |
| ۱۴ محصولات دسته | دسته باید وجود داشته باشد | `ProductService.getProductsByCategory` |
| ۱۵ افزودن به سبد | موجود بودن، سقف موجودی، افزایش تعداد | `CartService.addProduct`, `Cart.addProduct`, `Product.ensureCanBePurchased` |
| ۱۶ مشاهده سبد | جمع کل توسط سیستم | `CartService.getCart`, `Cart.getTotalAmount` |
| ۱۷ تغییر تعداد | حداقل ۱ و حداکثر موجودی | `CartService.changeQuantity`, `CartItem.changeQuantity` |
| ۱۸ حذف از سبد | محصول باید در سبد باشد | `CartService.removeProduct`, `Cart.removeProduct` |
| ۱۹ خالی کردن سبد | تعداد و مبلغ صفر | `CartService.clear`, `Cart.clear` |
| ۲۰ شروع خرید | سبد غیرخالی و محصولات هنوز موجود | `OrderService.startCheckout`, `Cart.ensureReadyForCheckout` |
| ۲۱ انتخاب آدرس | آدرس باید از آنِ همان کاربر باشد | `OrderService.placeOrder`, `User.getAddress` |
| ۲۲ مبلغ نهایی | محاسبه توسط سیستم، قابل تغییر نیست | `Order.getTotalAmount` (محاسبه در سازنده Order) |
| ۲۳ پرداخت | مبلغ = مبلغ سفارش؛ پرداخت مجدد ممنوع | `PaymentService.payOrder`, `Order.ensurePayable`, `Order.markAsPaid` |
| ۲۴ پرداخت ناموفق | وضعیت ناموفق، سفارش پرداخت‌نشده، تلاش مجدد | `Payment.markFailed`, `PaymentService.getPaymentsOfOrder` |
| ۲۵ تأیید پرداخت | بررسی نتیجه درگاه، مبلغ و تکراری نبودن | `PaymentService.verifyAndApply`, `PaymentGateway.charge` |
| ۲۶ مشاهده سفارش‌ها | تاریخچه با وضعیت | `OrderService.getOrdersOfUser` |
| ۲۷ جزئیات سفارش | فقط سفارش خود کاربر | `OrderService.getOrderOfUser`, `Order.ensureBelongsTo` |
| ۲۸ مشاهده موجودی | فقط مدیر | `InventoryService.getInventory`, `User.ensureAdmin` |
| ۲۹ تغییر موجودی | فقط مدیر، منفی نشود، ثبت سابقه | `InventoryService.adjustStock`, `Product.changeStock`, `StockChange` |
| ۳۰ ثبت نظر | فقط خریدار، متن غیرخالی و محدود | `ReviewService.addReview`, `Review` |
| ۳۱ امتیازدهی | فقط خریدار، ۱ تا ۵ | `ReviewService.rateProduct`, `Rating`, `Product.addRating` |
| ۳۲ مشاهده نظرات | فقط نام کوچک نمایش داده می‌شود | `ReviewService.getReviews` |
| ۳۳ ایجاد محصول | فقط مدیر، نام/قیمت/موجودی/دسته معتبر | `ProductService.createProduct` |
| ۳۴ ویرایش محصول | فقط مدیر | `ProductService.updateProduct`, `Product.update` |
| ۳۵ غیرفعال‌سازی | حذف نمی‌شود، سفارش‌های قبلی حفظ می‌شوند | `ProductService.deactivateProduct`, `Product.deactivate` |
| ۳۶ مدیریت سفارش‌ها | فقط مدیر، فیلتر وضعیت، بدون اطلاعات پرداخت | `OrderService.getAllOrders`, `OrderService.getOrderForAdmin` |

## ۶. تصمیم‌های طراحی مهم

- **ذخیره‌سازی در حافظه**: هر سرویس مالک داده‌های خودش است (`Map`)؛ سرویس‌های دیگر فقط از طریق متدهای آن سرویس به داده دسترسی دارند، پس تعویض با دیتابیس در فازهای بعد فقط روی سرویس‌ها اثر می‌گذارد.
- **امنیت رمز عبور**: رمز با PBKDF2 + salt تصادفی ذخیره می‌شود (`PasswordHasher` یک Interface است و قابل تعویض).
- **درگاه پرداخت**: `PaymentGateway` یک Port است و `SimulatedPaymentGateway` پیاده‌سازی آزمایشی (کارت‌های با رقم آخر ۰ رد می‌شوند). شماره کارت هرگز ذخیره یا نمایش داده نمی‌شود.
- **موجودی**: کم شدن موجودی هنگام «پرداخت موفق» انجام می‌شود و در تاریخچه (`StockChange`) ثبت می‌شود؛ پیش از پرداخت دوباره قابل‌خرید بودن اقلام بررسی می‌شود.
- **خطاها**: `ValidationException`، `NotFoundException`، `AccessDeniedException`، `BusinessRuleException` همگی از `DomainException` ارث می‌برند و CLI آن‌ها را به پیام مناسب تبدیل می‌کند.
