const socket = new SockJS('/ws');
const stompClient = Stomp.over(socket);

stompClient.connect({}, function(frame) {
    console.log('Connected: ' + frame);
});

function subscribeToProduct() {
    const productId = document.querySelector('#productId').value;
    stompClient.subscribe('/topic/product/' + productId, function(message) {
        document.querySelector("#updates").innerHTML += '<p>Product Update: ' + message.body + '</p>';
    });
}

function subscribeToCategory() {
    const categoryId = document.querySelector('#categoryId').value;
    stompClient.subscribe('/topic/category/' + categoryId, function(message) {
        document.querySelector("#updates").innerHTML += '<p>Category Update: ' + message.body + '</p>';
    });
}
