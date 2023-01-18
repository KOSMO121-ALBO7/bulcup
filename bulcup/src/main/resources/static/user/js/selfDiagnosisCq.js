$(document).ready(function(){

	var checkbox = $('input:checkbox');
	var checked = 0;

	checkbox.each(function () {
		$(this).click(function () {
			checked = 0;
			checkbox.each(function () {
				if ($(this).is(':checked')) checked++;
			})
			if (checked > 3) {
				$(this).prop('checked', false)
			}
		})
	})

});