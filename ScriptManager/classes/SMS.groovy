class SMS {
	public static def sms(String msg, String mobile="13858123802") {
		use(HTTP) {
			"https://service.toomao.com/sms/sendRemindMessage/${mobile}".post([message:msg])
		}
	}
}
