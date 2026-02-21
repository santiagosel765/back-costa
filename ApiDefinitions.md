# API Definitions

This document describes the backend endpoints and DTOs available in this service, covering authentication, inventory, and business operations.

## Category

### DTO `CategoryDTO`
| Field             | Type   | Description                                        |
|-------------------|--------|----------------------------------------------------|
| `id`              | UUID   | Identifier of the category                         |
| `name`            | String | Category name                                      |
| `description`     | String | Optional description                               |
| `parentCategoryId`| UUID   | Identifier of the parent category. `null` for root |
| `status`          | int    | 1 active, 0 disabled                               |

### POST `/v1/inventory/category/save`
Creates or updates a category.

Body: `CategoryDTO`

### POST `/v1/inventory/category/disable`
Disables a category.

Parameters: `id` (UUID)

### GET `/v1/inventory/categories`
Returns a paginated list of categories.

Query parameters:
- `page` – page number (default 0)
- `size` – page size (default 10)

Response: `PageResponse<CategoryDTO>`

---

## Product

### DTO `ProductDTO`
| Field         | Type   | Description                             |
|---------------|--------|-----------------------------------------|
| `id`          | UUID   | Identifier of the product               |
| `name`        | String | Product name                           |
| `description` | String | Description                            |
| `companyId`   | UUID   | Company identifier                      |
| `categoryId`  | UUID   | Category identifier                     |
| `status`      | int    | 1 active, 0 disabled                    |

### POST `/v1/inventory/product/save`
Creates or updates a product.

Body: `ProductDTO`

### POST `/v1/inventory/product/disable`
Disables a product.

Parameters: `id` (UUID)

### GET `/v1/inventory/products`
Returns a paginated list of products.

Query parameters:
- `page` – page number (default 0)
- `size` – page size (default 10)

Response: `PageResponse<ProductDTO>`

---

## Auth

### DTO `RegisterRequest`
| Field       | Type   | Description          |
|-------------|--------|----------------------|
| `username`  | String | Username for login   |
| `password`  | String | User password        |
| `email`     | String | Email address        |
| `fullName`  | String | Full name of user    |

### DTO `LoginRequest`
| Field      | Type   | Description        |
|------------|--------|--------------------|
| `username` | String | Username           |
| `password` | String | Password           |

### DTO `AuthResponse`
| Field     | Type   | Description                |
|-----------|--------|----------------------------|
| `token`   | String | JWT authentication token   |
| `username`| String | Authenticated username     |
| `email`   | String | User email                 |
| `role`    | String | Role name                  |

### POST `/v1/auth/register`
Registers a new user.

Body: `RegisterRequest`

Response: `AuthResponse`

### POST `/v1/auth/login`
Authenticates a user.

Body: `LoginRequest`

Response: `AuthResponse`

### POST `/v1/auth/change-password`
Changes a user's password.

Parameters:
- `newPassword` (String)
- `confirmPassword` (String)
- `userToken` (String)

Response: `AuthResponse`

### POST `/v1/auth/modules`
Returns modules accessible to the authenticated user.

Query parameters:
- `page` – page number (default 0)
- `size` – page size (default 10)

Response: `PageResponse<ModuleDTO>`

---

## Module

### DTO `ModuleDTO`
| Field        | Type   | Description                   |
|--------------|--------|-------------------------------|
| `id`         | UUID   | Module identifier             |
| `name`       | String | Module name                   |
| `description`| String | Module description            |
| `status`     | int    | 1 active, 0 disabled          |

### POST `/v1/modules/save`
Creates or updates a module.

Body: `ModuleDTO`

### GET `/v1/modules/list`
Returns a paginated list of modules.

Query parameters:
- `page` – page number (default 0)
- `size` – page size (default 10)

Response: `PageResponse<ModuleDTO>`

### POST `/v1/modules/disable`
Disables a module.

Parameters: `id` (UUID)

---

## Role

### DTO `RoleDTO`
| Field       | Type        | Description                          |
|-------------|-------------|--------------------------------------|
| `id`        | UUID        | Role identifier                      |
| `name`      | String      | Role name                            |
| `description`| String     | Description                          |
| `status`    | int         | 1 active, 0 disabled                 |
| `moduleIds` | List<UUID>  | Associated module identifiers        |

### POST `/v1/roles/save`
Creates or updates a role.

Body: `RoleDTO`

### POST `/v1/roles/list`
Returns a paginated list of roles.

Query parameters:
- `page` – page number (default 0)
- `size` – page size (default 10)

Response: `PageResponse<RoleDTO>`

### POST `/v1/roles/disable`
Disables a role.

Parameters: `roleId` (UUID)

---

## Client

### DTO `ClientDTO`
| Field     | Type   | Description               |
|-----------|--------|---------------------------|
| `id`      | UUID   | Client identifier         |
| `name`    | String | Client name               |
| `email`   | String | Email address             |
| `phone`   | String | Contact phone             |
| `address` | String | Physical address          |
| `status`  | int    | 1 active, 0 disabled      |

### POST `/v1/clients/save`
Creates or updates a client.

Body: `ClientDTO`

### POST `/v1/clients/disable`
Disables a client.

Parameters: `id` (UUID)

### GET `/v1/clients/list`
Returns a paginated list of clients.

Query parameters:
- `page` – page number (default 0)
- `size` – page size (default 10)

Response: `PageResponse<ClientDTO>`

---

## Provider

### DTO `ProviderDTO`
| Field     | Type   | Description                |
|-----------|--------|----------------------------|
| `id`      | UUID   | Provider identifier        |
| `name`    | String | Provider name              |
| `contact` | String | Contact person             |
| `phone`   | String | Contact phone              |
| `address` | String | Address                    |
| `ruc`     | String | Tax identifier             |
| `status`  | int    | 1 active, 0 disabled       |

### POST `/v1/providers/save`
Creates or updates a provider.

Body: `ProviderDTO`

### POST `/v1/providers/disable`
Disables a provider.

Parameters: `id` (UUID)

### GET `/v1/providers/list`
Returns a paginated list of providers.

Query parameters:
- `page` – page number (default 0)
- `size` – page size (default 10)

Response: `PageResponse<ProviderDTO>`

---

## Quote

### DTO `QuoteDetailDTO`
| Field        | Type      | Description             |
|--------------|-----------|-------------------------|
| `productId`  | UUID      | Quoted product ID       |
| `quantity`   | int       | Quantity quoted         |
| `unitPrice`  | BigDecimal| Price per unit          |

### DTO `QuoteDTO`
| Field       | Type                 | Description                    |
|-------------|----------------------|--------------------------------|
| `id`        | UUID                 | Quote identifier               |
| `clientId`  | UUID                 | Client identifier              |
| `description`| String              | Description                    |
| `date`      | LocalDate            | Quote date                     |
| `details`   | List<QuoteDetailDTO> | Line items                     |
| `total`     | BigDecimal           | Total amount                   |
| `status`    | int                  | 1 active, 0 disabled           |

### POST `/v1/quotes/save`
Creates or updates a quote.

Body: `QuoteDTO`

### POST `/v1/quotes/disable`
Disables a quote.

Parameters: `id` (UUID)

### GET `/v1/quotes/list`
Returns a paginated list of quotes.

Query parameters:
- `page` – page number (default 0)
- `size` – page size (default 10)

Response: `PageResponse<QuoteDTO>`

---

## Purchase

### DTO `PurchaseDetailDTO`
| Field        | Type      | Description             |
|--------------|-----------|-------------------------|
| `productId`  | UUID      | Purchased product ID    |
| `quantity`   | int       | Quantity purchased      |
| `unitPrice`  | BigDecimal| Price per unit          |

### DTO `PurchaseDTO`
| Field        | Type                   | Description                    |
|--------------|------------------------|--------------------------------|
| `id`         | UUID                   | Purchase identifier            |
| `providerId` | UUID                   | Provider identifier            |
| `description`| String                 | Description                    |
| `date`       | LocalDate              | Purchase date                  |
| `details`    | List<PurchaseDetailDTO>| Line items                     |
| `total`      | BigDecimal             | Total amount                   |
| `status`     | int                    | 1 active, 0 disabled           |

### POST `/v1/purchases/save`
Creates or updates a purchase.

Body: `PurchaseDTO`

### POST `/v1/purchases/disable`
Disables a purchase.

Parameters: `id` (UUID)

### GET `/v1/purchases/list`
Returns a paginated list of purchases.

Query parameters:
- `page` – page number (default 0)
- `size` – page size (default 10)

Response: `PageResponse<PurchaseDTO>`

---

## Pagination wrapper

### `PageResponse<T>`
Standard wrapper for paginated responses.

| Field          | Type      | Description                         |
|----------------|-----------|-------------------------------------|
| `content`      | List<T>   | Page elements                       |
| `totalPages`   | int       | Total number of pages               |
| `totalElements`| long      | Total number of elements            |
| `currentPage`  | int       | Current page number                 |
| `pageSize`     | int       | Size of the page                    |
