import time
import urllib.request
import ssl
import json
from imageai.Prediction import ImagePrediction


# noinspection DuplicatedCode
def gc_functions_handler(request):
	image = None

	ssl._create_default_https_context = ssl._create_unverified_context
	urllib.request.urlretrieve("https://github.com/fchollet/deep-learning-models/releases/download/v0.2"
							   "/resnet50_weights_tf_dim_ordering_tf_kernels.h5", "/tmp/model")

	if request.args.get('image') is not None:
		image = request.args.get('n')
	else:
		image = "https://upload.wikimedia.org/wikipedia/en/2/2d/Front_left_of_car.jpg"

	urllib.request.urlretrieve(image, "/tmp/image")

	start_time = time.time()

	prediction = ImagePrediction()
	prediction.setModelTypeAsResNet()
	prediction.setModelPath("/tmp/model")
	prediction.loadModel()

	result = ""

	predictions, percentage_probabilities = prediction.predictImage("/tmp/image", result_count=5)
	for index in range(len(predictions)):
		result = result + predictions[index] + ", "

	end_time = time.time()
	execution_time = (end_time - start_time) * 1000

	headers = {
		'Content-Type': 'application/json'
	}

	return (json.dumps({
		'success': True,
		'payload': {
			'test': 'image_classification',
			'image': image,
			'result': result,
			'milliseconds': execution_time
		}  # ,
		# 'cpu_info': {
		# 'model': cpu_model,
		# 'cores': cpu_cores
		# }
	}), 200, headers)
