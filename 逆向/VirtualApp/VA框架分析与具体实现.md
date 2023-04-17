# VA框架分析与具体实现

**VA中四大组件的架构**

**Service：**

![image](data:image/svg+xml;base64,PHN2ZyB2ZXJzaW9uPSIxLjEiIHhtbG5zPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwL3N2ZyIgeG1sbnM6eGxpbms9Imh0dHA6Ly93d3cudzMub3JnLzE5OTkveGxpbmsiIHN0eWxlPSJoZWlnaHQ6ODEuNjt3aWR0aDo1NTUuNjc7dGV4dC1hbGlnbjpsZWZ0OyIgdmlld0JveD0iMCAwIDU1NS42NyA4MS42Ij48cGF0aCBpZD0ncmVjdCcgZD0nTSAyIDIgTCA1NTMgMiBMIDU1MyA3OC45MyBMIDIgNzguOTMgWiAnIHN0eWxlPSJzdHJva2Utd2lkdGg6MC43NTtzdHJva2U6IzAwMDAwMDtmaWxsOiNGRkZGRkY7c3Ryb2tlLW9wYWNpdHk6MTtmaWxsLW9wYWNpdHk6MTsiICAvPjx0ZXh0IHg9JzEyLjgnIHk9JzIzLjInIHN0eWxlPSJmaWxsOiM1QzVDNUM7Zm9udC1zaXplOjE0cHg7Zm9udC1mYW1pbHk65a6L5L2TO3doaXRlLXNwYWNlOnByZTsiID48dHNwYW4gc3R5bGU9ImZpbGw6IzVDNUM1Qztmb250LXNpemU6MTRweDtmb250LWZhbWlseTrlrovkvZM7d2hpdGUtc3BhY2U6cHJlOyIgPmlmIChzZXJ2aWNlSW5mbyAhPSBudWxsKSB7PC90c3Bhbj48L3RleHQ+PHRleHQgeD0nMTIuOCcgeT0nNDIuOCcgc3R5bGU9ImZpbGw6IzVDNUM1Qztmb250LXNpemU6MTRweDtmb250LWZhbWlseTrlrovkvZM7d2hpdGUtc3BhY2U6cHJlOyIgPjx0c3BhbiBzdHlsZT0iZmlsbDojNUM1QzVDO2ZvbnQtc2l6ZToxNHB4O2ZvbnQtZmFtaWx5OuWui+S9kzt3aGl0ZS1zcGFjZTpwcmU7IiA+ICAgIHJldHVybiBWQWN0aXZpdHlNYW5hZ2VyLmdldCgpLnN0YXJ0U2VydmljZShhcHBUaHJlYWQsIHNlcnZpY2UsIHJlc29sdmVkPC90c3Bhbj48L3RleHQ+PHRleHQgeD0nMTIuOCcgeT0nNjIuNCcgc3R5bGU9ImZpbGw6IzVDNUM1Qztmb250LXNpemU6MTRweDtmb250LWZhbWlseTrlrovkvZM7d2hpdGUtc3BhY2U6cHJlOyIgPjx0c3BhbiBzdHlsZT0iZmlsbDojNUM1QzVDO2ZvbnQtc2l6ZToxNHB4O2ZvbnQtZmFtaWx5OuWui+S9kzt3aGl0ZS1zcGFjZTpwcmU7IiA+VHlwZSwgdXNlcklkKTs8L3RzcGFuPjwvdGV4dD48dGV4dCB4PScxMi44JyB5PSc4Micgc3R5bGU9ImZpbGw6IzVDNUM1Qztmb250LXNpemU6MTRweDtmb250LWZhbWlseTrlrovkvZM7d2hpdGUtc3BhY2U6cHJlOyIgPjx0c3BhbiBzdHlsZT0iZmlsbDojNUM1QzVDO2ZvbnQtc2l6ZToxNHB4O2ZvbnQtZmFtaWx5OuWui+S9kzt3aGl0ZS1zcGFjZTpwcmU7IiA+fTwvdHNwYW4+PC90ZXh0Pjwvc3ZnPg==)在第三方App调用startService（）来启动Service服务的时候，因为经过Hook startService，调用指向了ActivityManagerStub构建的startService结构，该结构最终通过ipc包下的VActivityManager与VA服务进程的VAMS通信，最终逻辑跳转到服务进程的VAMS，调用VActivityManagerService.startService()。

![image](data:image/svg+xml;base64,PHN2ZyB2ZXJzaW9uPSIxLjEiIHhtbG5zPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwL3N2ZyIgeG1sbnM6eGxpbms9Imh0dHA6Ly93d3cudzMub3JnLzE5OTkveGxpbmsiIHN0eWxlPSJoZWlnaHQ6ODEuNjt3aWR0aDo1NTUuNjc7dGV4dC1hbGlnbjpsZWZ0OyIgdmlld0JveD0iMCAwIDU1NS42NyA4MS42Ij48cGF0aCBpZD0ncmVjdCcgZD0nTSAyIDIgTCA1NTMgMiBMIDU1MyA3OC45MyBMIDIgNzguOTMgWiAnIHN0eWxlPSJzdHJva2Utd2lkdGg6MC43NTtzdHJva2U6IzAwMDAwMDtmaWxsOiNGRkZGRkY7c3Ryb2tlLW9wYWNpdHk6MTtmaWxsLW9wYWNpdHk6MTsiICAvPjx0ZXh0IHg9JzEyLjgnIHk9JzIzLjInIHN0eWxlPSJmaWxsOiM1QzVDNUM7Zm9udC1zaXplOjE0cHg7Zm9udC1mYW1pbHk65a6L5L2TO3doaXRlLXNwYWNlOnByZTsiID48dHNwYW4gc3R5bGU9ImZpbGw6IzUwQTE0Rjtmb250LXNpemU6MTRweDtmb250LWZhbWlseTrlrovkvZM7d2hpdGUtc3BhY2U6cHJlOyIgPlNlcnZpY2VJbmZvPC90c3Bhbj48dHNwYW4gc3R5bGU9ImZpbGw6IzVDNUM1Qztmb250LXNpemU6MTRweDtmb250LWZhbWlseTrlrovkvZM7d2hpdGUtc3BhY2U6cHJlOyIgPsKgc2VydmljZUluZm/CoD3CoHJlc29sdmVTZXJ2aWNlSW5mbyhzZXJ2aWNlLMKgdXNlcklkKTs8L3RzcGFuPjwvdGV4dD48dGV4dCB4PScxMi44JyB5PSc0Mi44JyBzdHlsZT0iZmlsbDojNUM1QzVDO2ZvbnQtc2l6ZToxNHB4O2ZvbnQtZmFtaWx5OuWui+S9kzt3aGl0ZS1zcGFjZTpwcmU7IiA+PHRzcGFuIHN0eWxlPSJmaWxsOiM1MEExNEY7Zm9udC1zaXplOjE0cHg7Zm9udC1mYW1pbHk65a6L5L2TO3doaXRlLXNwYWNlOnByZTsiID5Qcm9jZXNzUmVjb3JkPC90c3Bhbj48dHNwYW4gc3R5bGU9ImZpbGw6IzVDNUM1Qztmb250LXNpemU6MTRweDtmb250LWZhbWlseTrlrovkvZM7d2hpdGUtc3BhY2U6cHJlOyIgPsKgdGFyZ2V0QXBwwqA9wqBzdGFydFByb2Nlc3NJZk5lZWRMb2NrZWQoQ29tcG9uZW50VXRpbHMuZ2V0UHJvPC90c3Bhbj48L3RleHQ+PHRleHQgeD0nMTIuOCcgeT0nNjIuNCcgc3R5bGU9ImZpbGw6IzVDNUM1Qztmb250LXNpemU6MTRweDtmb250LWZhbWlseTrlrovkvZM7d2hpdGUtc3BhY2U6cHJlOyIgPjx0c3BhbiBzdHlsZT0iZmlsbDojNUM1QzVDO2ZvbnQtc2l6ZToxNHB4O2ZvbnQtZmFtaWx5OuWui+S9kzt3aGl0ZS1zcGFjZTpwcmU7IiA+Y2Vzc05hbWUoc2VydmljZUluZm8pLHVzZXJJZCxzZXJ2aWNlSW5mby5wYWNrYWdlTmFtZSk7PC90c3Bhbj48L3RleHQ+PC9zdmc+)在VAMS中可以看到，该结构先通过VPackageManagerServier来解析出ServiceInfo，依此创建Service进程。

![image](data:image/svg+xml;base64,PHN2ZyB2ZXJzaW9uPSIxLjEiIHhtbG5zPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwL3N2ZyIgeG1sbnM6eGxpbms9Imh0dHA6Ly93d3cudzMub3JnLzE5OTkveGxpbmsiIHN0eWxlPSJoZWlnaHQ6MTAwLjczO3dpZHRoOjU1NC43Mzt0ZXh0LWFsaWduOmxlZnQ7IiB2aWV3Qm94PSIwIDAgNTU0LjczIDEwMC43MyI+PHBhdGggaWQ9J3JlY3QnIGQ9J00gMiAyIEwgNTUyLjA3IDIgTCA1NTIuMDcgOTguMDcgTCAyIDk4LjA3IFogJyBzdHlsZT0ic3Ryb2tlLXdpZHRoOjAuNzU7c3Ryb2tlOiMwMDAwMDA7ZmlsbDojRkZGRkZGO3N0cm9rZS1vcGFjaXR5OjE7ZmlsbC1vcGFjaXR5OjE7IiAgLz48dGV4dCB4PScxMi44JyB5PScyMy4yJyBzdHlsZT0iZmlsbDojNUM1QzVDO2ZvbnQtc2l6ZToxNHB4O2ZvbnQtZmFtaWx5OuWui+S9kzt3aGl0ZS1zcGFjZTpwcmU7IiA+PHRzcGFuIHN0eWxlPSJmaWxsOiNFNDU2NDk7Zm9udC1zaXplOjE0cHg7Zm9udC1mYW1pbHk65a6L5L2TO3doaXRlLXNwYWNlOnByZTsiID5JQXBwbGljYXRpb25UaHJlYWRDb21wYXQ8L3RzcGFuPjx0c3BhbiBzdHlsZT0iZmlsbDojOTg2ODAxO2ZvbnQtc2l6ZToxNHB4O2ZvbnQtZmFtaWx5OuWui+S9kzt3aGl0ZS1zcGFjZTpwcmU7IiA+LnNjaGVkdWxlQ3JlYXRlU2VydmljZTwvdHNwYW4+PHRzcGFuIHN0eWxlPSJmaWxsOiM1QzVDNUM7Zm9udC1zaXplOjE0cHg7Zm9udC1mYW1pbHk65a6L5L2TO3doaXRlLXNwYWNlOnByZTsiID4oPC90c3Bhbj48dHNwYW4gc3R5bGU9ImZpbGw6I0U0NTY0OTtmb250LXNpemU6MTRweDtmb250LWZhbWlseTrlrovkvZM7d2hpdGUtc3BhY2U6cHJlOyIgPmFwcFRocmVhZDwvdHNwYW4+PHRzcGFuIHN0eWxlPSJmaWxsOiM1QzVDNUM7Zm9udC1zaXplOjE0cHg7Zm9udC1mYW1pbHk65a6L5L2TO3doaXRlLXNwYWNlOnByZTsiID4swqA8L3RzcGFuPjx0c3BhbiBzdHlsZT0iZmlsbDojRTQ1NjQ5O2ZvbnQtc2l6ZToxNHB4O2ZvbnQtZmFtaWx5OuWui+S9kzt3aGl0ZS1zcGFjZTpwcmU7IiA+cjwvdHNwYW4+PHRzcGFuIHN0eWxlPSJmaWxsOiM1QzVDNUM7Zm9udC1zaXplOjE0cHg7Zm9udC1mYW1pbHk65a6L5L2TO3doaXRlLXNwYWNlOnByZTsiID4swqA8L3RzcGFuPjx0c3BhbiBzdHlsZT0iZmlsbDojRTQ1NjQ5O2ZvbnQtc2l6ZToxNHB4O2ZvbnQtZmFtaWx5OuWui+S9kzt3aGl0ZS1zcGFjZTpwcmU7IiA+cjwvdHNwYW4+PHRzcGFuIHN0eWxlPSJmaWxsOiM5ODY4MDE7Zm9udC1zaXplOjE0cHg7Zm9udC1mYW1pbHk65a6L5L2TO3doaXRlLXNwYWNlOnByZTsiID4uc2VydmljZUluPC90c3Bhbj48L3RleHQ+PHRleHQgeD0nMTIuOCcgeT0nNDIuOCcgc3R5bGU9ImZpbGw6IzVDNUM1Qztmb250LXNpemU6MTRweDtmb250LWZhbWlseTrlrovkvZM7d2hpdGUtc3BhY2U6cHJlOyIgPjx0c3BhbiBzdHlsZT0iZmlsbDojOTg2ODAxO2ZvbnQtc2l6ZToxNHB4O2ZvbnQtZmFtaWx5OuWui+S9kzt3aGl0ZS1zcGFjZTpwcmU7IiA+Zm88L3RzcGFuPjx0c3BhbiBzdHlsZT0iZmlsbDojNUM1QzVDO2ZvbnQtc2l6ZToxNHB4O2ZvbnQtZmFtaWx5OuWui+S9kzt3aGl0ZS1zcGFjZTpwcmU7IiA+LMKgMCk7PC90c3Bhbj48L3RleHQ+PHRleHQgeD0nMTIuOCcgeT0nNjIuNCcgc3R5bGU9ImZpbGw6IzVDNUM1Qztmb250LXNpemU6MTRweDtmb250LWZhbWlseTrlrovkvZM7d2hpdGUtc3BhY2U6cHJlOyIgPjx0c3BhbiBzdHlsZT0iZmlsbDojRTQ1NjQ5O2ZvbnQtc2l6ZToxNHB4O2ZvbnQtZmFtaWx5OuWui+S9kzt3aGl0ZS1zcGFjZTpwcmU7IiA+SUFwcGxpY2F0aW9uVGhyZWFkQ29tcGF0PC90c3Bhbj48dHNwYW4gc3R5bGU9ImZpbGw6Izk4NjgwMTtmb250LXNpemU6MTRweDtmb250LWZhbWlseTrlrovkvZM7d2hpdGUtc3BhY2U6cHJlOyIgPi5zY2hlZHVsZVNlcnZpY2VBcmdzPC90c3Bhbj48dHNwYW4gc3R5bGU9ImZpbGw6IzVDNUM1Qztmb250LXNpemU6MTRweDtmb250LWZhbWlseTrlrovkvZM7d2hpdGUtc3BhY2U6cHJlOyIgPig8L3RzcGFuPjx0c3BhbiBzdHlsZT0iZmlsbDojRTQ1NjQ5O2ZvbnQtc2l6ZToxNHB4O2ZvbnQtZmFtaWx5OuWui+S9kzt3aGl0ZS1zcGFjZTpwcmU7IiA+YXBwVGhyZWFkPC90c3Bhbj48dHNwYW4gc3R5bGU9ImZpbGw6IzVDNUM1Qztmb250LXNpemU6MTRweDtmb250LWZhbWlseTrlrovkvZM7d2hpdGUtc3BhY2U6cHJlOyIgPizCoDwvdHNwYW4+PHRzcGFuIHN0eWxlPSJmaWxsOiNFNDU2NDk7Zm9udC1zaXplOjE0cHg7Zm9udC1mYW1pbHk65a6L5L2TO3doaXRlLXNwYWNlOnByZTsiID5yPC90c3Bhbj48dHNwYW4gc3R5bGU9ImZpbGw6IzVDNUM1Qztmb250LXNpemU6MTRweDtmb250LWZhbWlseTrlrovkvZM7d2hpdGUtc3BhY2U6cHJlOyIgPizCoDwvdHNwYW4+PHRzcGFuIHN0eWxlPSJmaWxsOiNFNDU2NDk7Zm9udC1zaXplOjE0cHg7Zm9udC1mYW1pbHk65a6L5L2TO3doaXRlLXNwYWNlOnByZTsiID50YXNrUmVtb3ZlZDwvdHNwYW4+PHRzcGFuIHN0eWxlPSJmaWxsOiM1QzVDNUM7Zm9udC1zaXplOjE0cHg7Zm9udC1mYW1pbHk65a6L5L2TO3doaXRlLXNwYWNlOnByZTsiID4swqA8L3RzcGFuPjwvdGV4dD48dGV4dCB4PScxMi44JyB5PSc4Micgc3R5bGU9ImZpbGw6IzVDNUM1Qztmb250LXNpemU6MTRweDtmb250LWZhbWlseTrlrovkvZM7d2hpdGUtc3BhY2U6cHJlOyIgPjx0c3BhbiBzdHlsZT0iZmlsbDojNUM1QzVDO2ZvbnQtc2l6ZToxNHB4O2ZvbnQtZmFtaWx5OuWui+S9kzt3aGl0ZS1zcGFjZTpwcmU7IiA+PC90c3Bhbj48dHNwYW4gc3R5bGU9ImZpbGw6I0U0NTY0OTtmb250LXNpemU6MTRweDtmb250LWZhbWlseTrlrovkvZM7d2hpdGUtc3BhY2U6cHJlOyIgPnI8L3RzcGFuPjx0c3BhbiBzdHlsZT0iZmlsbDojOTg2ODAxO2ZvbnQtc2l6ZToxNHB4O2ZvbnQtZmFtaWx5OuWui+S9kzt3aGl0ZS1zcGFjZTpwcmU7IiA+LnN0YXJ0SWQ8L3RzcGFuPjx0c3BhbiBzdHlsZT0iZmlsbDojNUM1QzVDO2ZvbnQtc2l6ZToxNHB4O2ZvbnQtZmFtaWx5OuWui+S9kzt3aGl0ZS1zcGFjZTpwcmU7IiA+LMKgMCzCoDwvdHNwYW4+PHRzcGFuIHN0eWxlPSJmaWxsOiNFNDU2NDk7Zm9udC1zaXplOjE0cHg7Zm9udC1mYW1pbHk65a6L5L2TO3doaXRlLXNwYWNlOnByZTsiID5zZXJ2aWNlPC90c3Bhbj48dHNwYW4gc3R5bGU9ImZpbGw6IzVDNUM1Qztmb250LXNpemU6MTRweDtmb250LWZhbWlseTrlrovkvZM7d2hpdGUtc3BhY2U6cHJlOyIgPik7PC90c3Bhbj48L3RleHQ+PC9zdmc+)然后在Process记录中查看该Service是否已经存在，如果不存在，则先通过IApplicationThreadCompat创建Service再调用scheduleServiceArgs，如果存在，则直接调用scheduleServiceArgs。这里的IApplicationThreadCompat是VA的一个封装类，在这个类中通过Client的IBinder来远程调用ApplicationThread中的scheduleCreateService等配置服务的相关方法。所以，关于Service的启动，VA的AMS并没有做实际处理，而是直接调用系统的接口来处理Service。

**Activity：**

当在VA中启动一个第三方App的时候，VA通过Hook让执行逻辑来到StartActivity类的call函数中，在这里先分析了调用者在参数中的各种数据，如Intent、resolvedType、resultTo等字段，然后通过VPMS解析目标ActivityInfo，远程调用VAMS中的startActivity。

**ActivityInfo** **activityInfo** **\=** **VirtualCore****.**_get_**()****.****resolveActivityInfo****(intent****,** **userId)****;**

**int** **res** **\=** **VActivityManager****.**_get_**()****.****startActivity****(intent****,** **activityInfo****,** **resultTo****,** **options****,** resultWho**,** requestCode**,** **VUserHandle****.**_myUserId_**())****;**

**public int** startActivity**(****Intent** intent**,** **ActivityInfo** info**,** **IBinder** resultTo**,** **Bundle** options**,** **String** resultWho**, int** requestCode**, int** userId**)** **{**      **try** **{**           **return** **getService****()****.****startActivity****(**intent**,** info**,** resultTo**,** options**,** resultWho**,** requestCode**,** userId**)****;**        **}** **catch** **(****RemoteException** e**)** **{**            **return** **VirtualRuntime****.**_crash_**(**e**)****;**        **}****}**

这里startActivity之所以会被指向VAMS的结构，是因为该方法被hook了，即在ActivityManagerStub实现的inject方法中，将原本的指向系统ActivityManagerNative替换为VA对其的动态代理对象，当调用startActivity的时候，就被hook到了MethodProxies类的startActivity内部类的call方法，最终还是通过VAM远程调用VAMS中的startActivity

方法。

在VAMS处理过程中，会通过ActivityStack的startActivityProcess方法替换Intent为真实的Intent，从而完成对第三方App的启动。

具体的替换思路是，预先在客户端进程中启动一个StubActivity，在实际需要启动Activity的时候，在startActivityProcess方法中，通过目标App的Pid找到对应的预先占坑的StubActivity，然后把真实Activity的Intent封装到预先占坑的StubActivity的Intent中，StubActivity的作用便是携带目标Activity的intent完成启动目标Activity。

在startActivityProcess中，先根据token找到源ActivityRecord和TaskRecord。

**ActivityRecord** **sourceRecord** **\=** **findActivityByToken****(**userId**,** resultTo**)****;**

**TaskRecord** **sourceTask** **\=** **sourceRecord** **!=** **null** **?** **sourceRecord****.****task** **:** **null****;**

然后分析其intent数据，处理其中的Flag。

然后根据目标Activity的启动模式，设置对应的clearTop、clearTarget、reuseTarget等等字段，由这里的reuseTarget再判断依靠哪种方式来设置reuseTask。

**switch** **(**info**.****launchMode)** **{**     **case** _**LAUNCH\_SINGLE\_TOP**_**:** **{**         singleTop **\=** **true****;**         **if** **(**_containFlags_**(**intent**,** **Intent****.****FLAG\_ACTIVITY\_NEW\_TASK****))** **{**             reuseTarget **\=** _containFlags_**(**intent**,** **Intent****.****FLAG\_ACTIVITY\_MULTIPLE\_TASK****)** **?** **ReuseTarget****.****MULTIPLE** **:** **ReuseTarget****.****AFFINITY;**         **}**      **}**

**…**

**}**

**switch** **(**reuseTarget**)** **{**    **case** **AFFINITY****:**        reuseTask **\=** **findTaskByAffinityLocked****(**userId**,** **affinity)****;**        **break****;**    **case** **DOCUMENT****:**        reuseTask **\=** **findTaskByIntentLocked****(**userId**,** intent**)****;**        **break****;**    **case** **CURRENT****:**        reuseTask **\=** **sourceTask****;**        **break****;**    **default****:**        **break****;****}**

如果该值为空，则直接在新栈启动目标Activity。如果不为空，则把目标Task移到前台，并对需要finish的Activity进行标记，之后会调用scheduleFinishMarkedActivityLocked来finish掉标记了的Activity。在这里还调用了deliverNewIntentLocked来触发onNewIntent来更新Intent数据。

**if** **(**reuseTask **\==** **null****)** **{**     **startActivityInNewTaskLocked****(**userId**,** intent**,** info**,** options**)****;**  **}** **else** **{**     **boolean** delivered **\=** **false****;**     **mAM****.****moveTaskToFront****(**reuseTask**.****taskId****,** **0****)****;**     **boolean** **startTaskToFront** **\= !****clearTask** **&& !**clearTop **&&** **ComponentUtils****.**_isSameIntent_**(**intent**,** reuseTask**.****taskRoot)****;**     **if** **(**clearTarget**.****deliverIntent** **||** singleTop**)** **{**         taskMarked **\=** **markTaskByClearTarget****(**reuseTask**,** clearTarget**,** intent**.****getComponent****())****;**         **ActivityRecord** **topRecord** **\=** _topActivityInTask_**(**reuseTask**)****;**         **if** **(**clearTop **&& !**singleTop **&&** **topRecord** **!=** **null** **&&** taskMarked**)** **{**             **topRecord****.****marked** **\=** **true****;**         **}**         **// Target activity is on top**         **if** **(topRecord** **!=** **null** **&& !****topRecord****.****marked** **&&** **topRecord****.****component****.****equals****(**intent**.****getComponent****()))** **{**            **deliverNewIntentLocked****(sourceRecord****,** **topRecord****,** intent**)****;**            delivered **\=** **true****;**         **}**     **}**     **if** **(**taskMarked**)** **{**         **synchronized** **(mHistory)** **{**             **scheduleFinishMarkedActivityLocked****()****;**         **}**     **}**

在做完关于的启动准备后，

最终，在系统AMS处理后，启动StubActivity的生命周期时，VA还需要做一些HOOK工作，首先，替换android.os.Handler.mCallback为自己的Handler（HCallbackStub类中）。

其次，还需要在调用Instrumentation.callActivityOnCreate时，对目标Activity的一些参数进行修正，因为如果不修正，则启动后的Activity的一些信息还是原本占坑的StubActivity。

**Provider：**

GetContentProvider：VA中，对getCententProvider的hook类，其call方法中通过对VAMS的相关调用，拉起Provider进程，然后替换为目标的Provider并返回给调用者。

VactivityManagerService.initProcess：初始化Provider进程，并返回其ProcessRecord给GetContentProvider.call函数，

StubContentProvider：类似Activity占坑的方式，VA构造了一个StubContentProvider类，用来给调用者提供Provider服务。与Activity中占坑不同的是，这里主要是为了初始化Provider的进程，并且通过call方法中返回其IBinder句柄给VAMS，来实现VAMS对Provider进程中方法的远程调用。

**return** **initProcess****(**extras**)****;**

**BundleCompat****.**_putBinder_**(res****,** **"\_VA\_|\_client\_"****,** **client****.****asBinder****())****;**

**res****.****putInt****(****"\_VA\_|\_pid\_"****,** **Process****.**_myPid_**())****;**

**return** **res****;**

在具体逻辑实现中，当调用getContentProvider的时候，VA在该客户端进程对其进行hook。首先是初始化Provider进程工作，VA在这里通过VPMS的resoleContentProvider拿到

ProviderInfo，

**ProviderInfo** info **\=** **VPackageManager****.**_get_**()****.****resolveContentProvider****(name****,** **0****,** **userId)****;**

然后再通过VAMS的initProcess方法调用StubContentProvider的call方法，来初始化Provider进程。

**int** **targetVPid** **\=** **VActivityManager****.**_get_**()****.****initProcess****(**info**.****packageName****,** info**.****processName****,** **userId)****;**

具体实现是，在VAMS的initProcess中先后通过startProcessIfNeedLocked、performStartProcessLocked等函数来获取StubContentProvider进程的ProcessRecord。

**ProcessRecord** **r** **\=** **startProcessIfNeedLocked****(**processName**,** userId**,** packageName**)****;**

app **\=** **performStartProcessLocked****(uid****,** **vpid****,** **info****,** processName**)****;**

**Bundle** **res** **\=****ProviderCall****.**_call_**(****VASettings****.**_getStubAuthority_**(**vpid**)****,****"\_VA\_|\_init\_process\_"****, null,** **extras)****;**

**ProcessRecord** **app** **\=** **new** **ProcessRecord****(**info**,** processName**,** vuid**,** vpid**)****;**

上述代码中的ProviderCall是一个VA的封装类，对其call调用会让执行逻辑来到ContentProviderCompat类的crazyAcquireContentProvider函数中，跟踪逻辑会发现这里最终调用了系统的acquireContentProviderClient方法来获取Provider客户端进程的句柄，并在ContentProviderCompat.call中对Provider的call进行调用，执行逻辑又来到了Provider桩——这个客户端进程中。

**ContentProviderClient** **client** **\=** _crazyAcquireContentProvider_**(**context**,** uri**)****;**

**return** context**.****getContentResolver****()****.****acquireContentProviderClient****(**uri**)****;**

res **\=** **client****.****call****(**method**,** arg**,** extras**)****;**

调用StubContentProvider的call方法，而在StubContentProvider的call中执行了该Stub自己的initProcess方法，最终返回一个Bundle，并且在这个bundle中放了client的IBinder句柄。

**VClientImpl** **client** **\=** **VClientImpl****.**_get_**()****;**

**client****.****initProcess****(token****,** **vuid)****;**

**Bundle** **res** **\=** **new** **Bundle****()****;**

**BundleCompat****.**_putBinder_**(res****,** **"\_VA\_|\_client\_"****,** **client****.****asBinder****())****;**

**res****.****putInt****(****"\_VA\_|\_pid\_"****,** **Process****.**_myPid_**())****;**

**return** **res****;**

因此，从触发getContentProvider的hook开始，执行逻辑再到VAMS的initProcess，再到系统的acquireContentProviderClient，再到目标StubContentProvider的call函数、initProcess函数。这个过程主要是初始化目标ContentProvider进程，并获取目标Provider客户端的句柄。获取之后，逻辑会回到performStartProcessLocked中，然后用拿到的目标Provider句柄来Attach客户端，返回该ProcessRecord，再由VAMS返回ProcessRecord的vpid给getCoentProvider的hook端。到这里，便完成了第一步——初始化Provider进程。

**attachClient****(pid****,** **clientBinder)****;**

**return** **app****;**

逻辑又回到了Hook逻辑中，在初始化了Provider进程并且拿到对方vpid后，开始第二步，进行provider代理替换。

先用上一步拿到的VPid等数据来获取实际目标对象的holder对象的provider字段。通过VAMS的ContentProviderHolder再拿到provider字段。

args**\[****nameIdx****\]** **\=** **VASettings****.**_getStubAuthority_**(targetVPid)****;**

**Object** **holder** **\=** method**.****invoke****(**who**,** args**)****;**

**IInterface** provider **\=** **IActivityManager****.****ContentProviderHolder****.**_**provider**_**.****get****(holder)****;**

然后将其provider字段进行替换。可以看到这里也调用了acquireProviderClient，不过这个不是Content ProviderCompat的acquireProviderClient,而是VactivityManager自己定义的方法，在该方法中直接通过反射获取了目标Provider，最终用该对象替换holder中的provider字段返回给调用者，最终完成整个provider的调用。

**public** **IInterface** acquireProviderClient**(****int** userId**,** **ProviderInfo**info**)** **throws** **RemoteException** **{**    **return** **ContentProviderNative****.**_**asInterface**_**.****call****(****getService****()****.****acquireProviderClient****(**userId**,** info**))****;** **}**

**if** **(**provider **!=** **null****)** **{**    provider **\=** **VActivityManager****.**_get_**()****.****acquireProviderClient****(userId****,** info**)****;**

**}**

**IActivityManager****.****ContentProviderHolder****.**_**provider**_**.****set****(holder****,** provider**)****;**

**IActivityManager****.****ContentProviderHolder****.**_**info**_**.****set****(holder****,** info**)****;**

**return** **holder****;**

**VA虚拟环境中的C/S架构**

对于VA的项目结构：

_**src.main.java.io.virtualapp**_：为宿主APP项目实现，自定义了Application：VApp，即整个程序的入口。

_**lib.src.main.java.com.lody.virtual**_：为VA虚拟环境的核心实现部分。client.core中实现了虚拟环境的初始化，如判断当前进程的类别从而有区别的进行函数HOOK。client.hook实现了对系统服务请求的具体Hook，如其中的am包即为对framework层的AMS的Hook。client.ipc仿造了系统framework client的一些服务类，内部App的请求会被重指向该结构而非系统的客户端层。client.NativeEngine类为Native层调用层，结合jni.Jni包实现了对native的hook。server包下是VA的服务进程对系统服务层的模拟，处理Hook后原本指向系统服务层的内部App请求，并负责与系统从进行通信，然后返回给内部App，如am包下为AMS的代理实现，VActivityManagerService类即为对AMS类的代理类。

_**lib.src.main.java.mirror**_：为系统framework的镜像层，VA通过反射实现对系统各个结构的镜像生成，再通过动态代理完成对各个服务的Hook。

VA采用多进程模式，主要有三个进程：io.virtualapp、io.virtualapp:pN、io.virtualapp:x。

io.virtualapp为整个VA的应用进程，相当于宿主进程。io.virtualapp:pN为内部APP启动后的进程，N代表数字序号，从0开始每启动一个内部APP，序号递增。io.virtualapp:x为VA的虚拟服务子进程，内部应用向系统framework层的所有请求都会被hook到该进程进行处理，再由该进程与系统服务层通信，最终返回给内部APP。

在VA初始化过程中，VA服务进程通过反射生成framework层服务的系统镜像，即mirror包，包括VAMS、VPMS、VWMS等等，然后由ServiceFetcher类将自己的各种服务接口暴露给客户端进程。当启动内部APP后，VA客户端进程通过动态代理的方式HOOK各个服务请求方法，并以IServiceFetcher获取虚拟服务层的各个IBinder，将内部APP的所有请求都指向服务进程虚拟的环境，再由服务进程代理与系统服务通信并返回给客户端。

具体实现中，启动一个内部APP，Client执行对AMS 的Hook，将原本向系统AMS的请求替换为指向VA虚拟代理的VAMS，VAMS处理 startActivity方法并请求系统AMS服务，系统服务返回后，VAMS向StubContentProvier 请求启动目标APP进程，VA的Activity栈管理调用startActivity，此时客户端Client 执行对HCallBackStub的Hook，使HCallBackStub通过intent将StubActivityRecord数据替换为真实数据，在通知VAMS调用onActivityCreate后，HCallBackStub通过handler启动目标Activity的生命周期。

**虚拟环境的创建**

当启动VA内部APP时，Client执行对AMS 的Hook，将原本向系统AMS的请求替换为指向VA虚拟代理的VAMS，VAMS处理 startActivity方法并请求系统AMS服务，系统服务返回后，VAMS向StubContentProvier 请求启动目标APP进程，VA的Activity栈管理调用startActivity，此时客户端Client 执行对HCallBackStub的Hook，使HCallBackStub通过intent将StubActivityRecord数据替换为为真实数据，在通知VAMS调用onActivityCreate后，HCallBackStub通过handler启动目标Activity的生命周期。

**Java hook**

**目的：**实现内部App的正常运行，VA需要模拟系统提供完整的framework层服务，当内部App访问系统服务时，进行hook并转向自己虚拟的service层，如果没有hook，则内部App就会直接访问系统的服务，从而代理失败。

**时机：**内部app在启动时，判断其进程类型为内部APP进程，添加相应需要HOOK的Stub，调用inject完成对内部App请求的hook。

**具体：**在InvocationStubManager类中初始化内部注入时，先判断当前进程类型，并添加相应的HOOK对象的Stub类，通过构造对应的Stub类定义具体需要hook的函数，然后调用该Stub类的inject实现hook。

构造时，在对应Stub类构造器中通过反射获取IActivityManager对象，并创建其动态代理。

**public** **ActivityManagerStub****()** **{**

    **super****(****new** **MethodInvocationStub****<>****(****ActivityManagerNative****.**_**getDefault**_**.****call****()))****;****}**

**public class** **ActivityManagerNative** **{**

    **public static** **Class****<?>**_**TYPE**_**\=****RefClass****.**_load_**(****ActivityManagerNative.** **class,** **"android.app.ActivityManagerNative"****)****;**    **public static** **RefStaticObject****<****Object****\>** _**gDefault**_**;**

最后在inject方法中将IActivityManager对象的代理通过反射进行动态替换

**Object** **gDefault** **\=****ActivityManagerNative****.**_**gDefault**_**.****get****()****;**

**Singleton****.**_**mInstance**_**.****set****(gDefault****,****getInvocationStub****()****.****getProxyInterface****())****;**

通过HOOK HCallbackStub 来实现msg消息拦截，进行数据替换。

**addInjector****(****HCallbackStub****.**_getDefault_**())****;**

**public void** inject**()** **throws** **Throwable** **{**

    **otherCallback** **\=** _getHCallback_**()****;**    **mirror.android.os.Handler****.**_**mCallback**_**.****set****(**_getH_**()****, this****)****;****}**

，以AMS的hook为例，hook包下的proxies.am为AMS hook的具体实现。当对Ams hook时，ActivityManagerStub解析注解添加需要hook的服务函数，如GetServices方法，该方法最终调用VActivityManager的getServices方法。

**static class** **GetServices** **extends** _**MethodProxy**_ **{**

**…**

    **return** **VActivityManager****.**_get_**()****.****getServices****(maxNum****,** **flags)****.****getList****()****;…}**

该方法将调用指向VA虚拟的framework服务层。

服务层利用mirror反射框架进行动态代理，从而实现VA对内部APP的服务代理

**public** **VParceledListSlice** getServices**(****int** maxNum**, int**

flags**)** **{**

**…**    **return** **getService****()****.****getServices****(**maxNum**,** flags**,** **VUserHandle****.**_myUserId_**())****;…****}**

**Native hook**

**目的：**虚拟App的文件访问重定向。当VA中的多个app同时访问IO时，会发生文件访问冲突，而且也没有做到各个App的数据隔离。所以需要在内部App访问文件时，将路径重新定位到VA虚拟的文件路径下，才能避免该类问题。大致原理为，在VA中启动各个APP时，通过Provider创建其进程，替换其Intent指向代理组件，同时动态代理系统的各个底层服务，当APP发送Intent时利用代理组件拦截并重定向到自己虚拟的服务层。

**NativeEngine****.**_redirectDirectory_**(userLibPath****,** **libPath)****;**

**if** **(****VASettings****.**_**ENABLE\_IO\_REDIRECT**_**)** **{**    **startIOUniformer****()****;****}**

**时机：**初始化时，调用InvocationStubManager类的injectInternal方法进行内部注入添加，判断如果当前为VApp进程，添加HCallbackStub.getDefault()所返回的对象的注入，此处返回的为一个HCallbackStub对象，该对象实现了Handler. Callback，在handleMessage中判断当massage为CREATE\_SERVICE时，调用VClientImpl的bindApplication方法。在Client包下的VClientImpl类中的bindApplicationNoCheck方法中调用startIOUniformer，该方法中调用NativeEngine类的方法进行文件路径重定向等操作。

**public static void** redirectDirectory**(****String** origPath**,** **String** newPath**)** **{**

**System****.**_loadLibrary_**(****"va++"****)****;**

**具体：**NativeEngine为JNI入口，调用相应本地方法实现相应重定向。Native文件为VAJni，以其方法jni\_nativeIORedirect为例

**static void** jni\_nativeIORedirect(alias\_ref<jclass\>

jclazz, jstring origPath, jstring newPath) {    ScopeUtfString orig\_path(origPath);    ScopeUtfString new\_path(newPath);    IOUniformer::redirect(orig\_path.c\_str(), new\_path.c\_str());}

注意到其构造器中初始化调用了IOUniformer::init\_env\_before\_all方法，该方法跳转到IOUniformer::startUniformer方法进行具体的native hook。

**if** (handle) {    HOOK\_SYMBOL(handle, faccessat);          HOOK\_SYMBOL(handle, \_\_openat);

native hook主要就是将libc库函数的方法进行Hook,将输入参数替换为我们的虚拟app路径。这里用了宏定义的方式，hook一个libc库中的方法时，主要分为两步：宏调用，宏定义。

#define HOOK\_SYMBOL(handle, func) hook\_function(handle,

#func, (**void**\*) new\_##func, (**void**\*\*) &orig\_##func)#define HOOK\_DEF(ret, func, ...) \  ret (\*orig\_##func)(\_\_VA\_ARGS\_\_); \  ret new\_##func(\_\_VA\_ARGS\_\_)

如HOOK linkat方法，HOOK\_DEF（ret，linkat，…）将linkat替换成ret (\*orig\_linkat)(\_\_VA\_ARGS\_\_)和ret new\_linkat(\_\_VA\_ARGS\_\_)

HOOK\_SYMBOL（handle，linkat）将调用linkat替换为hook\_function( handle, linkat, (void\*) new\_linkat, (void\*\*) &orig\_lintat ），hook\_function定义如下：

hook\_function(**void** \*handle, **const char** \*symbol, **void**

\*new\_func, **void** \*\*old\_func) {    **void** \*addr = dlsym(handle, symbol);    **if** (addr == NULL) {        **return**;    }    MSHookFunction(addr, new\_func, old\_func);}

最终调用SubstrateHook类的方法MSHookFunction(addr, new\_func, old\_func)实现底层hook。（该函数即为Cydia Substrate框架中的函数，SubstrateHook类是基于Inline Hook方式实现NativeHook的一种第三方开源项目。所有Native底层hook、

最终在该类中实现。）