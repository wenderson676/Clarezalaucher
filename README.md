# Clareza Launcher & VidaSimples Finanças

Aplicativo Android minimalista que combina uma tela inicial limpa (Launcher com foco em produtividade e bem-estar) a um gerenciador financeiro completo e determinístico offline-first com Room Database e Jetpack Compose.

---

## 🏗️ Arquitetura e Estrutura

- **Interface:** Jetpack Compose com Material Design 3 e suporte a cores dinâmicas.
- **Persistência de Dados:** SQLite local via **Room Database** com suporte a migrações versionadas determinísticas.
- **Gerenciamento de Estado:** ViewModel com Kotlin Coroutines, StateFlow e Flow reativo.
- **Atomicidade Financeira:** Operações com débitos e créditos sincronizados através de `database.withTransaction`.
- **Privacidade:** Banco de dados financeiro estritamente local; regras de backup configuradas em `data_extraction_rules.xml` e `backup_rules.xml` para isolar dados sensíveis de nuvens remotas sem criptografia.

---

## 🚀 Requisitos e Configuração

- **Linguagem:** Kotlin 2.0+
- **Android Gradle Plugin (AGP):** 9.1+
- **Gradle:** 9.3+
- **JDK:** Java 11 ou superior
- **Android SDK:** Min SDK 24 (Android 7.0), Target SDK 36 (Android 16)

---

## 🧪 Execução de Testes e Compilação

Para compilar o projeto:
```bash
gradle :app:assembleDebug
```

Para executar a suíte de testes unitários e de domínio:
```bash
gradle :app:testDebugUnitTest
```

Para verificar testes de screenshot (Roborazzi):
```bash
gradle :app:verifyRoborazziDebug
```

---

## 🗄️ Política de Migrações do Banco de Dados (Room)

O aplicativo utiliza migrações explícitas e **não utiliza `fallbackToDestructiveMigration()`** em produção para evitar perda de dados dos usuários:
- `MIGRATION_1_2`: Adiciona suporte a orçamentos (`budgets`) e metas (`financial_goals`).
- `MIGRATION_2_3`: Adiciona campos para controle de parcelas (`groupId`, `installmentNumber`, `totalInstallments`).
- `MIGRATION_3_4`: Adiciona status de efetivação (`isPaid`) para controle de pendências e lançamentos futuros.

---

## 🔒 Segurança e Google Play Policies

1. **Visibilidade de Pacotes:** Utiliza elementos `<queries>` no `AndroidManifest.xml` em conformidade com as diretrizes do Google Play para Launchers, dispensando a permissão irrestrita `QUERY_ALL_PACKAGES`.
2. **Proteção contra Overflow e Limites de Domínio:** Entradas numéricas e de dias de mês são validadas e limitadas contra overflow no `CurrencyFormatter`.
3. **Backup Deliberado:** O banco local financeiro (`vidasimples_database`) é excluído de backups desprotegidos em nuvem remota, mantendo transferência ponto-a-ponto entre dispositivos do usuário.
