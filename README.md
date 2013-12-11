Gitlab Mailer
-------------

This small application receives updates from Gitlab and pushes updates desired email addresses.


REST API:

POST /gitlab/:project

The above end-point expects Gitlab POST json as explained here: 
http://git.aliens-lyon.fr/help/web_hooks

Quick Test
curl -X POST -d @test-data.json --header "Content-Type: application/json" http://localhost:9000/gitlab/my_gitlab_project

