Mail Monster
------------

##A Gitlab projects Web Hook handler

This small application receives updates from Gitlab Web Hook and pushes updates to registered email accounts.

### REST API:

POST /gitlab/:project

The above end-point expects Gitlab JSon POSTs as explained here:
http://git.aliens-lyon.fr/help/web_hooks

### Quick Tests using curl:

- Send mail for project: curl -X POST -d @test-data.json --header "Content-Type: application/json" http://localhost:9000/rest/gitlab/my_gitlab_project
- List registered accouts (or use browser): curl -G http://localhost:9000/rest/db/accounts
- Update or create account: curl -X POST -d @test-account-update.json --header "Content-Type: application/json" http://localhost:9000/rest/db/account
- Delete account: curl -X DELETE --header "Content-Type: application/json" http://localhost:9000/rest/db/account/email_to_remove
