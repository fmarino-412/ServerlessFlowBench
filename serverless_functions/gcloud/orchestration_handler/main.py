import httplib2
import google.auth
import json
import time
from oauth2client.client import AccessTokenCredentials


# noinspection DuplicatedCode,PyUnusedLocal
def gc_functions_handler(request):
	workflow_name = ""
	access_token = ""

	# noinspection SpellCheckingInspection
	useragent = 'Mozilla/5.0 (iPad; CPU OS 6_0 like Mac OS X) AppleWebKit/536.26 (KHTML, like Gecko) Version/6.0 ' \
		'Mobile/10A5355d Safari/8536.25 '

	if request.args.get('token') is not None:
		access_token = request.args.get('token')
	else:
		return "Missing token"

	if request.args.get('workflow') is not None:
		workflow_name = request.args.get('workflow')
	else:
		return "Missing workflow"

	_, project_name = google.auth.default()

	url_slice1 = "https://workflowexecutions.googleapis.com/v1beta/"
	url_post_slice2 = "projects/"
	url_post_slice3 = "/locations/us-central1/workflows/"
	url_post_slice4 = "/executions"
	credentials = AccessTokenCredentials(access_token=access_token, user_agent=useragent)

	http = credentials.authorize(httplib2.Http())

	resp = json.loads((http.request(
		url_slice1 + url_post_slice2 + project_name + url_post_slice3 + workflow_name + url_post_slice4,
		method="POST"))[1])

	execution_name = resp.get('name')

	busy_wait(0.5)
	resp = json.loads((http.request(url_slice1 + execution_name, method="GET"))[1])

	while resp.get('state') == "ACTIVE":
		busy_wait(0.05)
		resp = json.loads((http.request(url_slice1 + execution_name, method="GET"))[1])

	if 'result' not in resp:
		return resp.get('state')
	else:
		return resp.get('result')


def busy_wait(dt):
	# to avoid scheduler decisions
	current_time = time.time()
	while time.time() < current_time + dt:
		continue
