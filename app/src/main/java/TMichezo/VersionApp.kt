package TMichezo

class VersionApp {
    var id : Int
    var latest : String
    var upgrade_type : UpgradeType = UpgradeType.None
    var description : String
    var created_at: String
    var updated_at : String

    constructor(id: Int, latest: String, upgrade_type: UpgradeType,
                description: String, created_at: String, updated_at: String) {
        this.id = id
        this.latest = latest
        this.upgrade_type = upgrade_type
        this.description = description
        this.created_at = created_at
        this.updated_at = updated_at
    }

}

/*
{"id":1,
"latest":"0.5.1",
"upgrade_type":"Minor",
"description":"First release",
"created_at":"2023-03-27T20:49:28.680665Z",
"updated_at":"2023-03-27T20:49:28.680665Z"}
 */