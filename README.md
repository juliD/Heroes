Code um in firebase pushnachrichten automatisch zu versenden:

'use strict';
const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp(functions.config().firebase);


//listens for datachages in every ask node
exports.sendPushAgent = functions.database.ref('/{ask}/{askid}/').onWrite(event => {
	console.log('Ask Push notification event triggered');
	if(!event.data.previous.exists()){
		//if item did not exist before
		return console.log('new ask offer created');	
	}
	const listItem = event.data.current.val();
	console.log(listItem);
	console.log(listItem.agent);
	
	const listItem_prev = event.data.previous.val();
	console.log(listItem_prev.agent);
	
	
	//get Tokens to push to
	const askid = event.params.askid;
	const ask = event.params.ask;
	console.log('Path ='+'/'+ask+'/'+askid+'/follower');
	const getUsersPromise = admin.database().ref('/'+ask+'/'+askid+'/follower').once('value');
	const getUserProfilePromise = admin.database().ref('/users/'+listItem.agent).once('value');
	const getAgentProfilePromise = admin.database().ref('/users/'+listItem.userid).once('value');
	
	
	return Promise.all([getUsersPromise,getUserProfilePromise,getAgentProfilePromise]).then(results => {
		const tokens = results[0];
		const user = results[1].val();
		const agent = results[2].val();
		if(!tokens.hasChildren()){
			return console.log('something went wrong');
		}
		console.log('There are', tokens.numChildren(), 'tokens to send notifications to.');
		
		//get Agent name
		const username = user.username;
		const language = agent.locale;
		console.log('language= '+language);
		let body = "";

		if(language=="Deutsch"){
			if(ask=="offer"){
				body = "hat dein Angebot angenommen!";
			}else{
				body = "has accepted your request!";
			}			
		}
		else{
			if(ask=="offer"){
				body = "has accepted your offer!";
			}else{
				body = "has accepted your request!";
			}
		}
		
		//listing all tokens
		let x = tokens.val();
		const t1 = x.owner;
		const t2 = x.agent;
		console.log('Token1: '+t1);
		console.log('Token2: '+t2);
		
		let alltokens = t2;
		let message = "";
		let type = "deine Anfrage";
		if(ask=="offer"){
			type = "dein Angebot";
		}	
		
		//check what data was set	
		//agent was added
		if(listItem.agent != listItem_prev.agent){
			console.log("neuer agent!");
			alltokens = t1;
			if(listItem.agent==""){
				return console.log('User hat Anfrage abgegeben');
			}	
			message = `${username} ${body}`;
		}
		//message was sent
		else{
			
			return console.log("Handled by a different function");
		}

		//what we actually want to send
		const payload = {
			notification: {
				title: 'Local Hero',
				body: message,
				sound: 'default',
				click_action: "MainActivity"
			},
			data: {
				title: 'Local Hero',
				body: `${username} ${type}`,
				sound: 'default',
			}
		};
		
		console.log('Tokens: '+alltokens);
		return admin.messaging().sendToDevice(alltokens,payload);
		
		
	});

});

//new message 
exports.sendPushMessage = functions.database.ref('/{ask}/{askid}/messages/{messageid}').onWrite(event => {
	console.log('New message push notification event triggered');
	
	//new data
	const m = event.data.current.val();
	let message = m.message;
	console.log(m);
	console.log(m.message);
	console.log(m.userid);
	
	
	
	
	//get Tokens to push to
	const askid = event.params.askid;
	const ask = event.params.ask;
	const getUsersPromise = admin.database().ref('/'+ask+'/'+askid).once('value');
	const getUserProfilePromise = admin.database().ref('/users/'+m.userid).once('value');

	
	return Promise.all([getUsersPromise,getUserProfilePromise]).then(results => {
		const itemData = results[0].val();
		const user = results[1].val();
		
		let t1 = itemData.follower.owner;
		let t2 = itemData.follower.agent;
		
		let alltokens = t1;
		
		//get who to notify
		if(itemData.userid == m.userid){
			console.log('Owner sent a message');
			alltokens = t2;
		}
		
		//get User Name
		let username = user.username;		
		
		message = username + ' : ' + message;

		//what we actually want to send
		const payload = {
			notification: {
				title: 'Neue Nachricht',
				body: message,
				sound: 'default',
				click_action:"MainActivity"
			},
			data: {
				title: 'Neue Nachricht',
				body: message,
				sound: 'default',
			}
		};
		
		console.log('Tokens: '+alltokens);
		return admin.messaging().sendToDevice(alltokens,payload);
		
		
	});

});



