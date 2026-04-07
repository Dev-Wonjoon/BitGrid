# 📦 BitGrid

**BitGrid** is a sophisticated storage management plugin for Minecraft servers that allows users to store items efficiently and intelligently. Using the "Storage Core" block, players can store vast quantities of items and manage them through an intuitive GUI with upgrade capabilities.

---

## ✨ Key Features

* **Storage Core**: A central block for high-capacity item storage.
* **Intelligent GUI**: Features an easy-to-use interface with item search and multiple sorting options (By Time, Amount, or ID).
* **Advanced Upgrade System**:
    * **Stack Upgrade**: Increases the maximum number of items allowed per slot.
    * **Page Upgrade**: Expands the total number of available pages in the storage.
* **Economy Integration**: Fully compatible with **Vault**, allowing players to pay for upgrades using experience or in-game currency.
* **Multi-language Support**: Supports both English and Korean by default, with customizable message files.

## 🛠 Installation

1.  Place the `BitGrid.jar` file into your server's `plugins` folder.
2.  Start the server to generate the default configuration files.
3.  (Optional) Install **Vault** to enable economy-based features.

## 📜 Commands & Permissions

### Commands
* `/bitgrid give <player>`: Grants a Storage Core block to a specific player.
* `/bitgrid invite <player>`: Invites a player to your storage grid.
* `/bitgrid kick <player>`: Removes a member from your storage grid.
* `/bitgrid members`: Lists all current members of your grid.
* `/bitgrid leave`: Leave your current storage grid.

### Permissions
* `bitgrid.use`: Allows usage of the Storage Core. (Default: true)
* `bitgrid.craft`: Allows crafting of the Storage Core. (Default: true)
* `bitgrid.admin`: Grants access to administrative commands. (Default: op)

## 💎 Crafting Recipe

The **Storage Core** can be crafted using the following default recipe (configurable in `config.yml`):

* **Ingredients**: Diamonds, Ender Pearls, and a Barrel.

| | | |
| :---: | :---: | :---: |
| Diamond | Ender Pearl | Diamond |
| Ender Pearl | **Barrel** | Ender Pearl |
| Diamond | Ender Pearl | Diamond |

## ⚙️ Configuration (`config.yml`)

You can customize database settings (SQLite/MySQL), upgrade costs, and storage limits within the `config.yml` file.

```yaml
database:
  type: sqlite
  host: localhost
  port: 3306
  name: bitgrid
  username: root
  password: ""

storage-core:
  crafting:
    enabled: true
    recipe:
      row1: ["DIAMOND", "ENDER_PEARL", "DIAMOND"]
      row2: ["ENDER_PEARL", "BARREL", "ENDER_PEARL"]
      row3: ["DIAMOND", "ENDER_PEARL", "DIAMOND"]

  upgrades:
    stack:
      max-level: 6
      price-type: "VAULT" # EXP or VAULT
      costs: [10, 20, 30, 40, 50, 60]
      limits: [256, 1024, 4096, 16384, 65536, 131072, 262144]
    page:
      max-level: 6
      price-type: "VAULT" # EXP or VAULT
      costs: [10, 20, 30, 40, 50, 60]
      limits: [3, 5, 7, 9, 11, 13, 15]
```