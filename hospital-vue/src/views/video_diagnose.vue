<template>
  <hr style="border-top: 3px solid #DEC9AB;">
  <div v-if="isAuth(['ROOT', 'VIDEO_DIAGNOSE:DIAGNOSE'])">
    <div class="main">

      <div id="remoteVideo">
        <div v-show="!showRemoteVideo">
          <img src="../../static/摄像头.png" class="camera"/>
          <p>患者摄像头</p>
        </div>
      </div>

      <div class="sidebar">

        <div id="localVideo">
          <div v-show="!showLocalVideo">
            <img src="../../static/摄像头.png" class="camera"/>
            <p style="color: black">医生端摄像头</p>
          </div>
        </div>

        <div class="operate">
          <input
              type="button"
              :class="status === 'offline' ? 'btn primary' : 'btn error'"
              :value="status === 'offline' ? '我要上线' : '立即下线'"
              @click="onlineOrOfflineHandle"
          />
          <input
              type="button"
              :class="open ? 'btn info' : 'btn success'"
              :value="open ? '关闭挂号' : '开放挂号'"
              @click="openOrCloseHandle"
          />
        </div>

        <div class="current-order">
          <h3>
            <SvgIcon name="camera_fill" class="icon-svg camera"/>
            当前问诊
          </h3>

          <div v-show="currentInfo.diagnoseId != null">
            <el-avatar class="avatars" shape="square" :size="45" :src="currentInfo.photo"/>

            <div class="infomations" style="margin: 20px">
              <div>姓名: {{ currentInfo.name }}</div>
              <div>电话: {{ currentInfo.tel }}</div>
              <div>用户状态: {{ currentInfo.status ? "正常" : "异常" }}</div>
              <div>用户状态: {{ currentInfo.status ? "正常" : "异常" }}</div>
              <div>结束时间：{{ currentInfo.expectEnd }}</div>
            </div>


            <!--            <div class="info" style="margin-top: 50px">-->
<!--            </div>-->

            <div class="timer-container">
							<span class="desc">
								距离
								<br/>
								{{ {'1': '开始', '2': '结束'}[currentStatus + ''] }}
							</span>
              <div class="timer">{{ countDownCard }}</div>
              <div class="clock">
                <el-icon :size="30" color="#777">
                  <AlarmClock/>
                </el-icon>
              </div>
            </div>

          </div>
          <div class="empty" v-show="currentInfo.diagnoseId == null">
            <el-empty description="暂无人问诊" image-size="85"/>
          </div>
        </div>





      </div>
<!--      <div class="next-order">
        <h3>
          <SvgIcon name="camera_fill" class="icon-svg camera"/>
          排队问诊
        </h3>
        <div v-show="nextInfo.diagnoseId != null">
          <div class="info">
            <el-avatar shape="square" :size="45" :src="nextInfo.photo"/>
            <ul>
              <li class="name">{{ nextInfo.name }}</li>
              <li class="tel">{{ nextInfo.tel }}</li>
            </ul>
          </div>
          <div class="time-range">
            <el-icon>
              <Clock/>
            </el-icon>
            <span>起始时间： {{ nextInfo.expectStart }} ~ {{ nextInfo.expectEnd }}</span>
          </div>
        </div>
        <div class="empty" v-show="nextInfo.diagnoseId == null">
          <el-empty description="无人问诊" image-size="35"/>
        </div>
      </div>-->

      <div class="data-container">
        <el-table
            :data="tableData"
            stripe
            border
            style="width: 100%"
            :header-cell-style="{ background: '#409eff', color: '#fff' }"
        >
          <el-table-column type="index" header-align="center" align="center" width="300" label="序号">
            <template #default="scope">
              <span>{{ scope.$index + 1 }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="date" label="日期" header-align="center" align="center"/>
<!--          <el-table-column prop="count" label="问诊人数" header-align="center" align="center"/>-->
        </el-table>
        <div id="chart"></div>
      </div>
    </div>

    <div class="images">
      <el-scrollbar>
        <div style="display: flex; height: 120px;">
          <el-image
              style="width: 100px; height: 100px;margin-right: 10px;flex-shrink: 0;flex-grow: 0;"
              v-for="one in imgList"
              :src="one"
              :preview-src-list="imgList"
              :initial-index="0"
              fit="cover"
          />
        </div>
      </el-scrollbar>
    </div>

  </div>
</template>

<script>
// import TRTC from 'trtc-js-sdk';
import {ElMessage} from "element-plus";
import dayjs from "dayjs";
import {TUICallEngine, TUICallEvent, TUICallType} from "tuicall-engine-webrtc";

export default {
  data() {
    return {
      status: 'offline',
      open: false,
      showLocalVideo: false,
      showRemoteVideo: false,
      appId: null,
      userSign: null,
      userId: null,
      roomId: null,
      client: null,
      localStream: null,
      remoteStream: null,
      // timerStyle: 'timer',
      currentOrder: null,
      currentStatus: 3,
      currentStart: null,
      currentEnd: null,
      currentInfo: {
        diagnoseId: null,
        name: null,
        tel: null,
        photo: null,
        expectStart: null,
        expectEnd: null,
        status: null
      },
      countDownCard: null,
      nextInfo: {
        diagnoseId: null,
        name: null,
        tel: null,
        photo: null,
        expectStart: null,
        expectEnd: null,
        status: null
      },
      // imgUrl: null,
      imgList: [],
      tableData: []
    };
  },
  methods: {
    onlineOrOfflineHandle() {
      if (this.status === 'offline') {
        this.$api.video.online().then(res => {
          this.status = 'online'
        })
      } else {
        this.$api.video.offline().then(res => {
          if (!res.result) {
            ElMessage({
              message: '有问诊患者，所以不能下线',
              type: 'warning',
              duration: 1200
            })
          } else {
            this.status = 'offline'
            this.open = false
          }
        })
      }
    },
    openOrCloseHandle() {
      if (this.status === 'offline') {
        ElMessage({
          message: '请先上线才能开放挂号',
          type: 'warning',
          duration: 1200
        })
        return

      }

      this.open = !this.open
      this.$api.video.updateOpenFlag(this.open, this.userId).then(res => {

      })
    },
    refreshInfo(isCatchMessage) {
      this.$api.video.refresh().then(res => {
        const {result} = res
        const {status} = result

        if (status === 'online') {
          this.status = 'online'
          this.open = result.open
          this.refreshDiagnoseInfo()
        }

      })
    },
    refreshDiagnoseInfo() {
      this.$api.video.searchVideoDiagnoseInfo().then(resp => {
        let result = resp.result

        if (resp.result.hasOwnProperty('currentInfo')) {

          this.currentInfo = resp.result.currentInfo
          this.currentStart = this.currentInfo.expectStart
          this.currentEnd = this.currentInfo.expectEnd

          if (dayjs(this.currentStart) >= dayjs()) {
            console.log('未开始')
            this.currentStatus = 1
          } else if (dayjs(this.currentEnd) < dayjs()) {
            console.log('已结束')
            this.currentStatus = 3
            this.currentInfo = {
              diagnoseId: null,
              name: null,
              tel: null,
              photo: null,
              expectStart: null,
              expectEnd: null,
              status: null
            }
          } else if (dayjs(this.currentStart) < dayjs()) {
            console.log('已开始')

            this.currentStatus = 2
          }

          if (this.imgList.length === 0 && this.currentStatus !== 3) {
            this.loadImage(this.currentInfo.diagnoseId)
          }

        } else {
          this.currentInfo = {
            diagnoseId: null,
            name: null,
            tel: null,
            photo: null,
            expectStart: null,
            expectEnd: null,
            status: null
          }
        }

        if (result.hasOwnProperty('nextInfo')) {
          const nextInfo = result.nextInfo
          nextInfo.expectStart = dayjs(nextInfo.expectStart).format('HH:mm')
          nextInfo.expectEnd = dayjs(nextInfo.expectEnd).format('HH:mm')
          this.nextInfo = nextInfo
          // if (nextInfo.nextPayment) {
          //
          // }

        } else {
          this.nextInfo = {
            diagnoseId: null,
            name: null,
            tel: null,
            photo: null,
            expectStart: null,
            expectEnd: null,
            status: null
          }
        }

        if (this.currentOrder !== 'none') {
          console.log('status:' + this.currentStatus)
          if (this.currentStatus === 1) {
            this.countDown()
          } else if (this.currentStatus === 2) {
            this.countDown()
          }
        }

      })
    },
    countDown() {
      let target = null

      if (this.currentStatus === 1) {
        target = this.currentStart
      } else if (this.currentStatus === 2) {
        target = this.currentEnd
      }

      if (this.timer === undefined) {
        this.timer = setInterval(() => {
          console.log('倒计时中...')
          if (dayjs(this.currentStart) >= dayjs()) {
            this.currentStatus = 1
            target = this.currentStart
          } else if (dayjs(this.currentStart) < dayjs()) {
            this.currentStatus = 2
            target = this.currentEnd
          }
          let now = dayjs()
          let seconds = Math.abs(dayjs(target).diff(now, 'second'))
          let minute = Math.floor(seconds / 60)
          if (minute < 10) {
            minute = '0' + minute
          }

          let second = seconds % 60
          if (second < 10) {
            second = '0' + second
          }

          if (this.currentStatus === 3) clearInterval(this.timer)
          if (minute === '00' && second === '00') {
            console.log('到时间了', this.currentStatus)
            switch (this.currentStatus) {
              case 1:
                this.loadImage(this.currentInfo.diagnoseId)
                break
              case 3:
                this.tuiCallEngine.hangup()
                this.currentInfo = {
                  diagnoseId: null,
                  name: null,
                  tel: null,
                  photo: null,
                  expectStart: null,
                  expectEnd: null,
                  status: null
                }
                this.imgList = []
                clearInterval(this.timer)
                break
            }


          } else {
            this.countDownCard = minute + ':' + second
          }

        }, 1000)
      }


    },
    loadImage(videoDiagnoseId) {
      this.imgList = []
      if (videoDiagnoseId === undefined) return
      this.$api.video.searchImageByVideoDiagnoseId(videoDiagnoseId).then(res => {
        const result = res.result

        for (let one of result) {
          this.imgList.push(`${this.$minioUrl}/${one.path}?random=${Math.random()}`)
        }
      })
    }

  },
  created: function () {
    this.loadImage()
    // setInterval(() => {
    //     this.refreshDiagnoseInfo()
    //     console.log('轮询中...')
    // }, 3000)

  },
  mounted: function () {
    this.status = 'offline'
    this.tuiCallEngine = null;
    this.$api.video.searchMyUserSig().then(res => {
      this.appId = res.appId
      this.userSig = res.userSig
      this.userId = res.userId + ''
      console.log('userId:' + this.userId)


      this.tuiCallEngine = TUICallEngine.createInstance({
        SDKAppID: this.appId
      });


      this.tuiCallEngine.login({userID: this.userId, userSig: this.userSig}).then(resp => {
        console.log('----------')
        console.log(resp)
        console.log('************')
      })

      this.tuiCallEngine.on(TUICallEvent.INVITED, event => {
        const {sponsor, isFromGroup, inviteData: {callType}} = event;
        const callTypeString = callType === TUICallType.VIDEO_CALL ? "Video" : "Voice";
        console.log(`${callTypeString} call from ${sponsor}`);
        this.tuiCallEngine.accept()
        this.showLocalVideo = true
        this.tuiCallEngine.openCamera("localVideo");
      })

      this.tuiCallEngine.on(TUICallEvent.USER_VIDEO_AVAILABLE, event => {
        const {userID, isVideoAvailable} = event;
        if (isVideoAvailable === true) { // 对方的摄像头可以渲染了

          this.showRemoteVideo = true
          this.tuiCallEngine.startRemoteView({userID, videoViewDomID: "remoteVideo"});
        }
      });

      this.tuiCallEngine.on(TUICallEvent.CALLING_END, event => {
        this.showRemoteVideo = false
        this.showLocalVideo = false
      });


    })

    this.$options.sockets.onmessage = (data) => {
      if (data.data === 'RefreshDiagnose' && this.currentStatus === 3) {
        this.refreshDiagnoseInfo()
      }
    }


    // TRTC.checkSystemRequirements().then(checkResult => {
    //     if (!checkResult.result) {
    //         this.$alert('当前浏览器不支持在线视频会诊', '提示信息', {
    //             confirmButtonText: '确定'
    //         })
    //     } else {
    //         TRTC.Logger.setLogLevel(TRTC.Logger.LogLevel.ERROR)
    //         this.$api.video.searchMyUserSig().then(res => {
    //             this.appId = res.appId
    //             this.userSig = res.userSig
    //             this.userId = res.userId
    //             console.log(this.userSig)
    //         })
    //
    //         const client = TRTC.createClient({
    //             mode: 'rtc',
    //             sdkAppId: this.appId,
    //             userId: this.userId + '',
    //             userSig: this.userSig,
    //             useStringRoomId: true
    //         })
    //
    //         this.client = client
    //
    //
    //     }
    // })

    this.refreshInfo(true)

  }
};
</script>

<style lang="less">
@import url('video_diagnose.less');
</style>
