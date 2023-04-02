package com.bqliang.running.diary.logic

import cn.leancloud.LCUser
import com.baidu.trace.api.entity.AddEntityRequest
import com.baidu.trace.api.entity.AddEntityResponse
import com.baidu.trace.api.entity.OnEntityListener
import com.bqliang.running.diary.BAIDU_TRACE_SERVICE_ID
import com.bqliang.running.diary.RunningDiaryApp
import com.bqliang.running.diary.database.RunningDiaryDB
import com.bqliang.running.diary.database.entity.Body
import com.bqliang.running.diary.database.entity.Path
import com.bqliang.running.diary.database.entity.Point
import com.bqliang.running.diary.database.entity.Run
import com.bqliang.running.diary.utils.shortId
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class Repository @Inject constructor(private val db: RunningDiaryDB) {


    /**
     * 添加 LC User
     *
     * @param username 用户名
     * @param email 邮箱
     * @param password 密码
     * @return 添加 LC User's short id 的 flow
     */
    private fun addLcUser(username: String, email: String, password: String): Flow<String> = flow {

        val shortId: String = suspendCoroutine { continuation ->
            val user = LCUser()
            user.username = username
            user.email = email
            user.password = password

            user.signUpInBackground().subscribe(object : Observer<LCUser> {
                override fun onSubscribe(disposable: Disposable) {
                }

                override fun onError(throwable: Throwable) {
                    // 注册失败
                    continuation.resumeWithException(throwable)
                }

                override fun onComplete() {
                }

                override fun onNext(lcUser: LCUser) {
                    // 注册成功
                    continuation.resume(lcUser.shortId)
                }
            })
        }

        emit(shortId)
    }


    /**
     * 添加 Entity
     *
     * @param entityName Entity Name
     * @return 添加 Entity 响应的 flow
     */
    private fun addEntity(entityName: String): Flow<AddEntityResponse> = flow {
        val addEntityRequest = AddEntityRequest().apply {
            setEntityName(entityName)
            serviceId = BAIDU_TRACE_SERVICE_ID
        }

        val addEntityResponse = suspendCoroutine() { continuation ->
            RunningDiaryApp.lbsTraceClient.addEntity(addEntityRequest,
                object : OnEntityListener() {
                    override fun onAddEntityCallback(addEntityResponse: AddEntityResponse) {
                        super.onAddEntityCallback(addEntityResponse)
                        if (addEntityResponse.status == 0) {
                            continuation.resume(addEntityResponse)
                        } else {
                            val errorDetails =
                                with(addEntityResponse) { "status: $status, message: $message, tag: $tag" }
                            continuation.resumeWithException(Exception(errorDetails))
                        }
                    }
                })
        }

        emit(addEntityResponse)
    }


    /**
     * Register User
     *
     */
    @OptIn(FlowPreview::class)
    fun register(username: String, email: String, password: String) =
        addLcUser(username, email, password)
            .flatMapConcat { shortId ->
                addEntity(shortId)
                    .onEach { addEntityResponse ->
                        if (addEntityResponse.status != 0) {
                            throw Exception("Add Entity Failed")
                        }
                    }
            }


    suspend fun insertRun(run: Run) = db.getRunDao().insertRun(run)
    suspend fun insertPaths(paths: List<Path>) = db.getPathDao().insertPaths(paths)
    suspend fun insertPoint(points: List<Point>) = db.getPointDao().insertPoints(points)

    fun getRunById(id: Long) = db.getRunDao().getRunById(id)
    suspend fun updateRunNoteById(note:String, id: Long) = db.getRunDao().updateNoteById(note, id)
    fun getPointsByRunId(runId: Long) = db.getPointDao().getPointsByRunId(runId)
    fun getPathsByRunId(runId: Long) = db.getPathDao().getPathsByRunId(runId)
    fun getPointsByPaths(path: List<Path>): Flow<List<Point>> {
        val pathIds = path.map { it.id }
        return db.getPointDao().getPointsByPathIds(pathIds)
    }

    suspend fun insertBody(body: Body) = db.getBodyDao().insertBody(body)
    fun getBodyById(id: Long) = db.getBodyDao().getBodyById(id)
    suspend fun updateBody(newBody: Body) = db.getBodyDao().updateBody(newBody)
    fun getAllBodyByEntityName(entityName: String) = db.getBodyDao().getAllBodyByEntityName(entityName)
}