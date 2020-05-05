### promise的用法
* Promise是异步编程的一种解决方案.
* Promise本质上也是一个Observable，能使用fromPromise把Promise转成Observable 
* 但是Promise .then()只能返回一个值，Observable可以返回多个值。
* Promise要么resolve要么reject，并且只响应一次。而Observable可以响应多次。
* Promise不能取消，Observable可以调用unsubscribe()取消订阅。
* Pending(进行中)、Resolved(已完成)、Rejected(已失败)
var promise = new Promise(function(resolve,reject){
    // 这里所有方法都是异步的
  // ... some code
  if(/* 异步操作成功 */){
    resolve(value);
  }else{
    reject(error);
  }
});
promise.then(
    // 这里的方法就是resolve的定制
    function(value){
  // sucess
},// 这里的方法就是reject的定制
function(error){
  // failure
});
* 用Promise对象实现Ajax操作的例子：
var getJSON = function(url){
  var promise = new Promise(function(resolve,reject){
    var client = new XMLHttpRequest();
    client.open('GET',url);
    client.onreadystatechange = handler;
    client.responseType = 'json';
    client.setRequestHeader('Accept','application/json');
    client.send();
    function handler(){
      if(this.readyState !== 4){
        return;
      }
      if(this.status === 200){
        resolve(this.response);
      }else{
        reject(new Error(this.statusText));
      }
    }
  });

  return promise;
};
getJSON('/posts.jons').then(function(json){
  consoloe.log(json);
},function(error){
  console.log('出错了');
});

### RxJS入门，异步线程调度，让操作一步步的执行，观察者模式
var observable = Rx.Observable
// 通过create方法创建一个Observable
// 回调函数会接受observer参数，也就是观察者角色
	.create(function(observer) {
		observer.next('hi');
		observer.next('world');

        setTimeout(() => {
			observer.next('这一段是异步操作');
		}, 30)
	})

// 订阅这个 observable
// 只有在订阅之后，才会在流Observable变化的时候，调用observer提供的方法，并通知他	
// 订阅之后也可以取消订阅，调用unsubscribe()即可
console.log('start')
var subscription = observable.subscribe(function(value) {
	console.log(value);
})
console.log('end')
setTimeOut(()=> {
  subscription.unsubscribe()
}, 5000)

### Redux用法，订阅者模式，类似于eventbus数据透传
React的传递数据是一级一级地传的，传递给另外一个组件的时候，需要先将数据传递给两个组件的父组件，再由父组件传递给另外一个组件，耗性能，耗时间，效率低。
Redux（数据调度）：会给需要传递的数据建立一个sotre，然后由store来派发给另外一个组件。
* 创建Store.js，store 是全局的，任何一个 react 组件想用它都可以引入进去。
import { createStore } from 'redux';
import Reducer from './Reducer';
const store = createStore(Reducer)
export default store;
* 新建Reducer.js，执行Action的具体逻辑
export default (state = 0, action) => {
  switch (action.type) {
    case ActionTypes.ADD:
      return state + 1;
    default:
      return state;
  }
};
* 新建 ActionTypes.js 放需要派发的动作类型例如：export const ADD = 'add'
* 新建 Action.js
export const add = () => {
  return {
      type: ActionTypes.ADD
  };
};
* 具体页面：
import React, { Component } from "react";
import store from "./Store";
import action from "./Action";

export default class App extends Component {
    constructor(props) {
    super(props);

    this.state = {
        count: store.getState()
    };
  }

  render() {
   store.subscribe(() => {
     this.setState({
      count: store.getState()
     })
   }));

    return (
      <div className="container">
       <h1 className="text-center mt-5">{store.getState()}</h1>
        <p className="text-center">
          <button className="btn btn-primary mr-2" onClick={store.dispatch(action.add());}>
            add
          </button>
        </p>
      </div>
    );
  }
}







