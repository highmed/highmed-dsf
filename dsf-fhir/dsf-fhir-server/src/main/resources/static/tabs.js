function openTab(lang)
{
	var i, tabcontent, tablinks;
	tabcontent = document.getElementsByClassName("prettyprint");
	for (i = 0; i < tabcontent.length; i++)
	{
		tabcontent[i].style.display = "none";
	}
	
	tablinks = document.getElementsByClassName("tablinks");
	for (i = 0; i < tablinks.length; i++)
	{
		tablinks[i].className = tablinks[i].className.replace(" active", "");
	}
	
	document.getElementById(lang).style.display = "block";
	document.getElementById(lang + "-button").className += " active";
	
	if (localStorage != null)
		localStorage.setItem('lang', lang);
}

function openInitialTab()
{
	var lang = localStorage != null && localStorage.getItem("lang") != null ? localStorage.getItem("lang") : "xml";
	if (lang == "xml" || lang == "json")
		openTab(lang);
}