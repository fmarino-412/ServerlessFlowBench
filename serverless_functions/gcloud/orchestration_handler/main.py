import httplib2
from oauth2client.client import AccessTokenCredentials


# noinspection DuplicatedCode
def gc_functions_handler(request):
	workflow_name = ""
	access_token = ""
	project_name = ""

	useragent = 'Mozilla/5.0 (iPad; CPU OS 6_0 like Mac OS X) AppleWebKit/536.26 (KHTML, like Gecko) Version/6.0 ' \
				'Mobile/10A5355d Safari/8536.25 '

	if request.args.get('token') is not None:
		access_token = request.args.get('token')
	else:
		return

	if request.args.get('workflow') is not None:
		workflow_name = request.args.get('workflow')
	else:
		return

	if request.args.get('project') is not None:
		project_name = request.args.get('project')
	else:
		return

	url_slice1 = "https://workflowexecutions.googleapis.com/v1beta/projects/"
	url_slice2 = "/locations/us-central1/workflows/"
	url_slice3 = "/executions"
	credentials = AccessTokenCredentials(access_token=access_token, user_agent=useragent)

	http = credentials.authorize(httplib2.Http())

	resp = http.request(url_slice1 + project_name + url_slice2 + workflow_name + url_slice3,
						method="POST")[1]

	return resp.decode()
