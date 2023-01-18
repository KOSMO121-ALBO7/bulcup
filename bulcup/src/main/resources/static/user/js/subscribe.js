$(document).ready(function() {
	$('#sub').click(function() {
		let regex = new RegExp('[a-z0-9]+@[a-z]+\.[a-z]{2,3}');
		var email = $('#subs-email').val().replace(" ", "");

		if (regex.test(email)) {
			Swal.fire({
				icon: 'warning',
				title: "구독 신청 여부",
				text: "정말 구독 신청을 하시겠습니까?",
				showCancelButton: true, // cancel버튼 보이기. 기본은 원래 없음
				confirmButtonColor: '#335E4A', // confrim 버튼 색깔 지정
				cancelButtonColor: '#d33', // cancel 버튼 색깔 지정
				confirmButtonText: '신청', // confirm 버튼 텍스트 지정
				cancelButtonText: '취소', // cancel 버튼 텍스트 지정
			}).then(result => {
				if (result.isConfirmed) { // 만일 confirm 버튼을 눌렀다면  
					$.ajax({
						type: "POST",
						url: 'insertSubscriber.do',
						data: { email: $('#subs-email').val() },
						success: function(result) {
							Swal.fire('구독신청이 완료되었습니다.');
						},	 // end of success function
						error: function(err) {
							Swal.fire({
								icon: 'error',
								title: "서비스 이용 불가",
								text: "이용에 불편을 드려 죄송합니다.",
								confirmButtonColor: '#335E4A', // confrim 버튼 색깔 지정   							
							});
							console.log(err);
						} // end of error function
					}); // end of ajax
				} // end of isConfirmed
			})
		} else {
			Swal.fire({
				title: "이메일을 정확히 입력해주세요.",
				confirmButtonColor: '#335E4A', // confrim 버튼 색깔 지정

			})
		} // end of if-else

	});	// end of click function
})