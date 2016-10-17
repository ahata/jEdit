import cn.toomao.http.*

class HTTP {
	public static def get(String url, Map params=[:], Map headers=[:]) {
		def reqUrl = RestClient.buildUri(url, params)
		RestClient.getDefault().get(reqUrl, headers)
	}

	public static def post(String url, Map data=[:], Map params=[:], Map headers=[:]) {
		def reqUrl = RestClient.buildUri(url, params)
		RestClient.getDefault().post(reqUrl, headers, data)
	}
}
