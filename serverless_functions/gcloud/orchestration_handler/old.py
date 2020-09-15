import httplib2
import google.auth


# noinspection DuplicatedCode
def gc_functions_handler(request):
	workflow_name = "my_workflow"
	url_slice1 = "https://workflowexecutions.googleapis.com/v1beta/projects/"
	url_slice2 = "/locations/us-central1/workflows/"
	url_slice3 = "/executions"
	credentials, project = google.auth.default()

	http = httplib2.Http()
	#http = credentials.authorize(http)

	resp = http.request(url_slice1 + "Containers" + url_slice2 + workflow_name + url_slice3,
						method="POST")[1]

	return resp.decode()