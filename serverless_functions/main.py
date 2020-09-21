import urllib.request as request


def test():
	url = "http://imagesvc.meredithcorp.io/v3/mm/image?url=https%3A%2F%2Fstatic.onecms.io%2Fwp-content%2" \
		  "Fuploads%2Fsites%2F20%2F2020%2F08%2F11%2Fjordin-sparks-instagram.jpg"

	useragent = 'Mozilla/5.0 (iPad; CPU OS 6_0 like Mac OS X) AppleWebKit/536.26 (KHTML, like Gecko) Version/6.0 ' \
				'Mobile/10A5355d Safari/8536.25 '

	r = request.Request(url, headers={'User-Agent': useragent})
	f = request.urlopen(r)
	print(bytearray(f.read()))


# Press the green button in the gutter to run the script.
if __name__ == '__main__':
	test()

# See PyCharm help at https://www.jetbrains.com/help/pycharm/
