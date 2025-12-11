# Wallet Service API

A secure wallet management system with Paystack integration for processing deposits, transfers, and withdrawals.

## Features

### Authentication & Security
- Google OAuth 2.0 authentication
- JWT token-based sessions
- API key management with granular permissions
- Bcrypt-hashed credentials and API keys
- Webhook signature verification

### Wallet Management
- Automatic wallet creation on user registration
- Unique 10-digit wallet numbers
- Real-time balance tracking
- Transaction history with pagination
- Support for multiple transaction types

### Transaction Processing
- **Deposits**: Paystack-powered wallet funding
- **Transfers**: Peer-to-peer wallet transfers
- **Withdrawals**: Direct fund withdrawals (JWT-only)
- Automatic webhook processing for payment confirmations
- Transaction status tracking

### API Key System
- Create up to 5 active API keys per user
- Configurable permissions: deposit, transfer, read, withdraw
- Flexible expiry options: 1H, 1D, 1M, 1Y
- Rollover expired keys with same permissions
- Instant revocation for compromised keys

### Security & Permissions
- JWT users have full access including withdrawals
- API keys can perform deposits, transfers, and reads only
- Withdrawals restricted to JWT authentication for security
- Method-level authorization with Spring Security

## Tech Stack

- Spring Boot 3.5.7
- Java 21
- PostgreSQL with JPA/Hibernate
- JWT Authentication (jjwt 0.12.7)
- Google OAuth 2.0
- Paystack Payment Gateway
- OpenAPI 3.0 Documentation
- Maven Build Tool

## Environment Variables

```env
# Database Configuration
DB_URL=jdbc:postgresql://localhost:5432/walletdb
DB_USERNAME=your_database_username
DB_PASSWORD=your_database_password

# JWT Configuration
JWT_SECRET=your_secret_key_minimum_32_characters

# Google OAuth Configuration
GOOGLE_CLIENT_ID=your_google_client_id
GOOGLE_CLIENT_SECRET=your_google_client_secret
GOOGLE_REDIRECT_URI=http://localhost:8080/api/v1/auth/google/callback

# Paystack Configuration
PAYSTACK_SECRET_KEY=your_paystack_secret_key
```

## Getting Started

1. Clone the repository
2. Set up PostgreSQL database
3. Configure environment variables
4. Run the application using Maven:
    - Build: `./mvnw clean package`
    - Run: `./mvnw spring-boot:run`
5. Access the API at `http://localhost:8080`

## API Documentation

Interactive API documentation is available at:

**http://localhost:8080/docs**

The documentation includes:
- Complete endpoint reference
- Request/response schemas
- Authentication examples
- Interactive testing interface

## Key Endpoints

- **Authentication**: `/api/v1/auth/google`, `/api/v1/auth/google/callback`
- **Wallet Operations**: `/api/v1/wallet/deposit`, `/api/v1/wallet/transfer`, `/api/v1/wallet/withdraw`, `/api/v1/wallet/balance`, `/api/v1/wallet/transactions`
- **API Keys**: `/api/v1/keys/create`, `/api/v1/keys`, `/api/v1/keys/{keyId}/revoke`
- **Webhooks**: `/api/v1/wallet/paystack/webhook`

## Important Notes

- All amounts are in kobo (1 Naira = 100 kobo)
- Minimum deposit: 10,000 kobo (100 NGN)
- Minimum withdrawal: 5,000 kobo (50 NGN)
- Minimum transfer: 100 kobo (1 NGN)
- Database schema auto-created on first run
- Use HTTPS in production environments

## Support

For issues and questions:
- Email: slethware@gmail.com
- Create an issue in the repository

## License

MIT License