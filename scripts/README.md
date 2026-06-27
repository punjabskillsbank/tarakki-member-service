## Git Hooks Setup

To maintain a clean and consistent Git history during development, we enforce branch and commit naming conventions.

### Branch Naming

Use the following format:

```text
feature/TK_<TICKET_ID>
bugfix/TK_<TICKET_ID>
```

### Commit Message Format

Use the following format:

```text
TK_<TICKET_ID>: Your commit message
```

### Validation Rules

- Branch name must follow the specified format.
- Commit message must start with a valid ticket number.
- The ticket number in the commit message must match the ticket number in the branch name.

#### Example

Branch:

```text
feature/TK_<TICKET_ID>
```

Valid Commit:

```text
TK_<TICKET_ID>: Add login API
```

Invalid Commit:

```text
Add login API
```

### Setup

After cloning the repository, run the setup script once:

**Linux/macOS**

```bash
chmod +x scripts/setup-hooks.sh
./scripts/setup-hooks.sh
```

**Windows**

```cmd
scripts\setup-hooks.bat
```

This configures Git to use the project's shared hooks and enables local validation before commits are created.

In addition to local hooks, GitHub Actions also validate these rules during pushes and pull requests to ensure consistency across the repository.
