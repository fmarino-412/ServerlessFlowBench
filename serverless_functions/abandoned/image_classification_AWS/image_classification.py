import time
import urllib.request
import ssl
from imageai.Prediction import ImagePrediction
import json


# noinspection DuplicatedCode
def lambda_handler(event, context):
	image = None

	ssl._create_default_https_context = ssl._create_unverified_context
	urllib.request.urlretrieve("https://github.com/fchollet/deep-learning-models/releases/download/v0.2"
							   "/resnet50_weights_tf_dim_ordering_tf_kernels.h5", "/tmp/model")

	if event.get('queryStringParameters') is not None:
		if 'image' in event['queryStringParameters']:
			image = (event['queryStringParameters']['image'])
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

	return {
		'statusCode': 200,
		'headers': {
			'Content-Type': 'application/json'
		},
		'body': json.dumps({
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
		})
	}
