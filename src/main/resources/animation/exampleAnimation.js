function animate(str, primary, accent){//any script file should have function named 'animate' which is the entry point of script
	//first param(required) - it's the original string that is not yet animated. (the Value: part of scoreboard.yml)
	//second+ params(optional) - the parameters that you defined in scoreboard.yml. (primary = &7, accent = &4 for this example)
	primary = animate.color(primary);
	accent = animate.color(accent);
	
	//array of string that will be shown
	//animation order will follow the index of array
	var strs = [];
	
	for(var i = 0; i < str.length; i++){
		var temp = primary;
		
		for(var pos = 0; pos < str.length; pos++){
			if(pos == i){
				temp += accent + str[pos] + primary;
			}else{
				temp += str[pos];
			}
		}
		
		strs.push(temp);
	}
	
	strs.push(primary+str);
	strs.push(accent+str);
	strs.push(primary+str);
	strs.push(accent+str);
	
	//finally, just return the array
	return strs;
}

animate.color = function(str){//a simple function to convert color code
	var ChatColor = Java.type('org.bukkit.ChatColor');
	return ChatColor.translateAlternateColorCodes('&', str);
}