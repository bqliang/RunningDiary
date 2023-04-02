package com.bqliang.running.diary.ui.course

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bqliang.running.diary.databinding.FragmentCourseListBinding
import com.bqliang.running.diary.ui.course.model.Course
import com.bqliang.running.diary.ui.course.model.FitnessAction
import com.bqliang.running.diary.utils.navController

class CourseListFragment : Fragment() {

    private var _binding: FragmentCourseListBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        _binding = FragmentCourseListBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val courseList = initCourseList()
        val adapter = CourseAdapter(courseList) { course ->
            navController.navigate(
                CourseListFragmentDirections.actionCourseListFragmentToCourseDetailsFragment(
                    course
                )
            )
        }

        binding.recyclerView.adapter = adapter
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    private fun initCourseList(): List<Course> = mutableListOf(
        preRunWarmUpCourse(),
        stretchingAfterRun(),

    )


    private fun preRunWarmUpCourse() = Course(
        "https://bqliang-1304284634.cos.ap-guangzhou.myqcloud.com/%E8%B7%91%E6%AD%A5%E6%97%A5%E8%AE%B0/%E8%B7%91%E5%89%8D%E7%83%AD%E8%BA%AB/%E8%B7%91%E5%89%8D%E7%83%AD%E8%BA%AB.mp4",
        "https://sporthealth-drcn.dbankcdn.com/nsp-sporthealth-oper-drcn/configCenter/20200901/2EDFB10E85584D882ED4F489AE8AE829.jpg",
        "跑前热身",
        "L1 入门",
        6,
        32,
        "唤醒身体，快速进入跑步状态",
        listOf(
            FitnessAction(
                "肩向后环绕",
                "1x20''  休息 0 秒",
                "https://bqliang-1304284634.cos.ap-guangzhou.myqcloud.com/%E8%B7%91%E6%AD%A5%E6%97%A5%E8%AE%B0/%E8%B7%91%E5%89%8D%E7%83%AD%E8%BA%AB/01%E8%82%A9%E5%90%91%E5%90%8E%E7%8E%AF%E7%BB%95.mp4",
                """
                    要点
                    ·双手放在肩部
                    ·以肩关节为轴心，最大幅度地向后画圆 
                    ·腰腹核心收紧，保持身体稳定 
                    
                    呼吸
                    ·自然呼吸 
                    
                    动作感觉 
                    ·肩膀前侧有轻微拉伸感 
                    
                    常见错误
                    ·环绕幅度不够 
                    ·身体松散
                """.trimIndent()
            ),
            FitnessAction(
                "臀部动态拉伸",
                "1x36''  休息 0 秒",
                "https://bqliang-1304284634.cos.ap-guangzhou.myqcloud.com/%E8%B7%91%E6%AD%A5%E6%97%A5%E8%AE%B0/%E8%B7%91%E5%89%8D%E7%83%AD%E8%BA%AB/02%E8%87%80%E9%83%A8%E5%8A%A8%E6%80%81%E6%8B%89%E4%BC%B8.mp4",
                """
                    要点 
                    ·抬膝同时双手抱住膝盖尽量让其靠近胸腹 
                    ·保持身体平衡稳定， 左右腿交替进行 
                    
                    呼吸 
                    ·按自己的节奏呼吸 动作感觉 
                    ·臀部有拉伸感，小腿有紧绷发力感 
                    
                    常见错误 
                    ·抬腿时身体后仰
                """.trimIndent()
            ),
            FitnessAction(
                "左侧动态弓步压腿",
                "1x20''  休息 0 秒",
                "https://bqliang-1304284634.cos.ap-guangzhou.myqcloud.com/%E8%B7%91%E6%AD%A5%E6%97%A5%E8%AE%B0/%E8%B7%91%E5%89%8D%E7%83%AD%E8%BA%AB/03%E5%B7%A6%E4%BE%A7%E5%8A%A8%E6%80%81%E5%BC%93%E6%AD%A5%E5%8E%8B%E8%85%BF.mp4",
                """
                    要点 
                    ·向前跨一大步,双手放在膝盖上 
                    ·上身保持直立,轻快弹动压腿 
                    
                    呼吸 
                    ·保持自然的呼吸 
                    
                    动作感觉 
                    ·左腿膝盖部位肌肉有发力紧绷感， 右腿胯部有牵拉感 
                    
                    常见错误 
                    ·上半身前倾太多 
                    ·膝盖超过了脚尖
                """.trimIndent()
            ),
            FitnessAction(
                "右侧动态弓步压腿",
                "1x20''  休息 0 秒",
                "https://bqliang-1304284634.cos.ap-guangzhou.myqcloud.com/%E8%B7%91%E6%AD%A5%E6%97%A5%E8%AE%B0/%E8%B7%91%E5%89%8D%E7%83%AD%E8%BA%AB/04%E5%8F%B3%E4%BE%A7%E5%8A%A8%E6%80%81%E5%BC%93%E6%AD%A5%E5%8E%8B%E8%85%BF.mp4",
                """
                    要点 
                    ·向前跨一大步,双手放在膝盖上 
                    ·上身保持直立,轻快弹动压腿 
                    
                    呼吸 
                    ·保持自然的呼吸 
                    
                    动作感觉 
                    ·左腿膝盖部位肌肉有发力紧绷感， 右腿胯部有牵拉感 
                    
                    常见错误 
                    ·上半身前倾太多 
                    ·膝盖超过了脚尖
                """.trimIndent()
            ),
            FitnessAction(
                "左侧动态侧弓步压腿",
                "1x20''  休息 0 秒",
                "https://bqliang-1304284634.cos.ap-guangzhou.myqcloud.com/%E8%B7%91%E6%AD%A5%E6%97%A5%E8%AE%B0/%E8%B7%91%E5%89%8D%E7%83%AD%E8%BA%AB/05%E5%B7%A6%E4%BE%A7%E5%8A%A8%E6%80%81%E4%BE%A7%E5%BC%93%E6%AD%A5%E5%8E%8B%E8%85%BF.mp4",
                """
                    要点 
                    ·向侧方跨一大步,双手放在膝盖上 
                    ·背部保持挺直,轻快弹动拉腿 
                    
                    呼吸 
                    ·保持自然的呼吸 
                    
                    动作感觉 
                    ·右大腿内侧有牵引感， 左侧膝盖部位肌肉有发力紧绷感
                    
                    常见错误 
                    ·出现弯腰弓背
                    ·膝盖出现内扣
                """.trimIndent()
            ),
            FitnessAction(
                "右侧动态侧弓步压腿",
                "1x20''  休息 0 秒",
                "https://bqliang-1304284634.cos.ap-guangzhou.myqcloud.com/%E8%B7%91%E6%AD%A5%E6%97%A5%E8%AE%B0/%E8%B7%91%E5%89%8D%E7%83%AD%E8%BA%AB/06%E5%8F%B3%E4%BE%A7%E5%8A%A8%E6%80%81%E4%BE%A7%E5%BC%93%E6%AD%A5%E5%8E%8B%E8%85%BF.mp4",
                """
                    要点 
                    ·向侧方跨一大步,双手放在膝盖上 
                    ·背部保持挺直,轻快弹动拉腿 
                    
                    呼吸 
                    ·保持自然的呼吸 
                    
                    动作感觉 
                    ·左大腿内侧有牵引感， 右侧膝盖部位肌肉有发力紧绷感
                    
                    常见错误 
                    ·出现弯腰弓背
                    ·膝盖出现内扣
                """.trimIndent()
            ),
            FitnessAction(
                "开合跳",
                "1x20''  休息 0 秒",
                "https://bqliang-1304284634.cos.ap-guangzhou.myqcloud.com/%E8%B7%91%E6%AD%A5%E6%97%A5%E8%AE%B0/%E8%B7%91%E5%89%8D%E7%83%AD%E8%BA%AB/07%E5%BC%80%E5%90%88%E8%B7%B3.mp4",
                """
                    要点
                    ·头看前方，腰腹收紧 
                    ·双腿打开同时，双手在头上交叉 
                    ·落地时要轻快 
                    
                    呼吸 
                    ·保持均匀呼吸 
                    
                    动作感觉 
                    ·核心收紧，整个身体有弹动感 
                    
                    常见错误 
                    ·跳太高，落地不够轻盈 
                    ·腹部松垮
                """.trimIndent()
            ),
            FitnessAction(
                "勾腿跑",
                "1x20''  休息 0 秒",
                "https://bqliang-1304284634.cos.ap-guangzhou.myqcloud.com/%E8%B7%91%E6%AD%A5%E6%97%A5%E8%AE%B0/%E8%B7%91%E5%89%8D%E7%83%AD%E8%BA%AB/08%E5%8B%BE%E8%85%BF%E8%B7%91.mp4",
                """
                    要点 
                    ·双手交叉在后 
                    ·跑起来时，勾脚去碰手 
                    ·轻快放松的弹动 
                    
                    呼吸 
                    ·保持自然的呼吸 动作感觉 
                    ·弹动轻盈， 心跳逐渐加速 
                    
                    常见错误 
                    ·弹动不够轻盈 
                    ·身体过度前倾
                """.trimIndent()
            ),
            FitnessAction(
                "原地快跑",
                "1x20''  休息 0 秒",
                "https://bqliang-1304284634.cos.ap-guangzhou.myqcloud.com/%E8%B7%91%E6%AD%A5%E6%97%A5%E8%AE%B0/%E8%B7%91%E5%89%8D%E7%83%AD%E8%BA%AB/09%E5%8E%9F%E5%9C%B0%E5%BF%AB%E8%B7%91.mp4",
                """
                    要点
                    ·上身稍微前倾 
                    ·两臂贴着身体快速摆臂，前脚掌着地原地快速跑 
                    ·躯干尽量保持稳定 
                    
                    呼吸 
                    ·保持自然的呼吸 
                    
                    动作感觉 
                    ·核心收紧全身用力，心跳和呼吸越来越快 
                    
                    常见错误 
                    ·脚落地没有控制，声音太大
                """.trimIndent()
            ),
            FitnessAction(
                "大腿前侧动态拉伸",
                "1x38''  休息 0 秒",
                "https://bqliang-1304284634.cos.ap-guangzhou.myqcloud.com/%E8%B7%91%E6%AD%A5%E6%97%A5%E8%AE%B0/%E8%B7%91%E5%89%8D%E7%83%AD%E8%BA%AB/10%E5%A4%A7%E8%85%BF%E5%89%8D%E4%BE%A7%E5%8A%A8%E6%80%81%E6%8B%89%E4%BC%B8.mp4",
                """
                   要点 
                   ·身体保持真立，依次交替拉伸腿前侧 
                   ·髋部往前前顶， 感觉会更强烈 
                   ·手拉脚踝而非脚尖 
                   
                   呼吸 
                   ·保持自然的呼吸 
                   
                   动作感觉 
                   ·大腿前侧有拉伸感，小腿有紧绷发力感 
                   
                   常见错误 
                   ·上半身前倾或弯腰
                """.trimIndent()
            ),
        )
    )


    private fun stretchingAfterRun() = Course(
        "https://bqliang-1304284634.cos.ap-guangzhou.myqcloud.com/%E8%B7%91%E6%AD%A5%E6%97%A5%E8%AE%B0/%E8%B7%91%E5%90%8E%E6%8B%89%E4%BC%B8/%E8%B7%91%E5%90%8E%E6%8B%89%E4%BC%B8.mp4",
        "https://sporthealth-drcn.dbankcdn.com/nsp-sporthealth-oper-drcn/configCenter/20200829/07936943BB6814C0616911D5984E6964.jpg",
        "跑后拉伸",
        "L1 入门",
        6,
        15,
        "缓解身体疲劳，预防关节和肌肉损伤",
        listOf(
            FitnessAction(
                "左侧肩背拉伸",
                "1x20''  休息 0 秒",
                "https://bqliang-1304284634.cos.ap-guangzhou.myqcloud.com/%E8%B7%91%E6%AD%A5%E6%97%A5%E8%AE%B0/%E8%B7%91%E5%90%8E%E6%8B%89%E4%BC%B8/01%E5%B7%A6%E4%BE%A7%E8%82%A9%E8%83%8C%E6%8B%89%E4%BC%B8.mp4",
                """
                    要点 
                    ·左手尽量往右伸
                    ·用右手扣住左臂 
                    
                    呼吸 
                    ·保持均匀呼吸 
                     
                    动作感觉
                    ·左侧大臂后侧和肩后侧有拉伸感
                    
                    常见错误 
                    ·手伸的幅度不够
                """.trimIndent()
            ),
            FitnessAction(
                "右侧肩背拉伸",
                "1x20''  休息 0 秒",
                "https://bqliang-1304284634.cos.ap-guangzhou.myqcloud.com/%E8%B7%91%E6%AD%A5%E6%97%A5%E8%AE%B0/%E8%B7%91%E5%90%8E%E6%8B%89%E4%BC%B8/01%E5%B7%A6%E4%BE%A7%E8%82%A9%E8%83%8C%E6%8B%89%E4%BC%B8.mp4",
                """
                    要点 
                    ·右手尽量往左伸
                    ·用左手扣住右臂 
                    
                    呼吸 
                    ·保持均匀呼吸 
                     
                    动作感觉
                    ·右侧大臂后侧和肩后侧有拉伸感
                    
                    常见错误 
                    ·手伸的幅度不够
                """.trimIndent()
            ),
            FitnessAction(
                "左侧腿后侧拉伸",
                "1x30''  休息 0 秒",
                "https://bqliang-1304284634.cos.ap-guangzhou.myqcloud.com/%E8%B7%91%E6%AD%A5%E6%97%A5%E8%AE%B0/%E8%B7%91%E5%90%8E%E6%8B%89%E4%BC%B8/03%E5%B7%A6%E4%BE%A7%E8%85%BF%E5%90%8E%E4%BE%A7%E6%8B%89%E4%BC%B8.mp4",
                """
                    要点 
                    ·脚前迈一小步，上身向下压
                    ·背部打直，脚跟着地
                    ·膝盖伸直，脚尖回勾
                    
                    呼吸
                    ·保持均匀呼吸 
                    
                    动作感觉 
                    ·大腿后侧有拉伸感
                    
                    常见错误 
                    ·出现弓背的情况
                """.trimIndent()
            ),
            FitnessAction(
                "右侧腿后侧拉伸",
                "1x30''  休息 0 秒",
                "https://bqliang-1304284634.cos.ap-guangzhou.myqcloud.com/%E8%B7%91%E6%AD%A5%E6%97%A5%E8%AE%B0/%E8%B7%91%E5%90%8E%E6%8B%89%E4%BC%B8/04%E5%8F%B3%E4%BE%A7%E8%85%BF%E5%90%8E%E4%BE%A7%E6%8B%89%E4%BC%B8.mp4",
                """
                    要点 
                    ·脚前迈一小步，上身向下压
                    ·背部打直，脚跟着地
                    ·膝盖伸直，脚尖回勾
                    
                    呼吸
                    ·保持均匀呼吸 
                    
                    动作感觉 
                    ·大腿后侧有拉伸感
                    
                    常见错误 
                    ·出现弓背的情况
                """.trimIndent()
            ),
            FitnessAction(
                "左侧大腿前侧拉伸",
                "1x30''  休息 0 秒",
                "https://bqliang-1304284634.cos.ap-guangzhou.myqcloud.com/%E8%B7%91%E6%AD%A5%E6%97%A5%E8%AE%B0/%E8%B7%91%E5%90%8E%E6%8B%89%E4%BC%B8/05%E5%B7%A6%E4%BE%A7%E5%A4%A7%E8%85%BF%E5%89%8D%E4%BE%A7%E6%8B%89%E4%BC%B8.mp4",
                """
                    要点 
                    ·身体站立，用手拉住脚踝
                    `保持住平衡，髋部向前顶
                    
                    呼吸
                    ·保持均匀呼吸
                    
                    动作感觉 
                    ·大腿前侧有拉伸感，小腿有紧绷发力感
                    
                    常见错误 
                    ·上半身前倾，没有髋部向前顶
                """.trimIndent()
            ),
            FitnessAction(
                "右侧大腿前侧拉伸",
                "1x30''  休息 0 秒",
                "https://bqliang-1304284634.cos.ap-guangzhou.myqcloud.com/%E8%B7%91%E6%AD%A5%E6%97%A5%E8%AE%B0/%E8%B7%91%E5%90%8E%E6%8B%89%E4%BC%B8/06%E5%8F%B3%E4%BE%A7%E5%A4%A7%E8%85%BF%E5%89%8D%E4%BE%A7%E6%8B%89%E4%BC%B8.mp4",
                """
                    要点 
                    ·身体站立，用手拉住脚踝
                    `保持住平衡，髋部向前顶
                    
                    呼吸
                    ·保持均匀呼吸
                    
                    动作感觉 
                    ·大腿前侧有拉伸感，小腿有紧绷发力感
                    
                    常见错误 
                    ·上半身前倾，没有髋部向前顶
                """.trimIndent()
            ),
            FitnessAction(
                "扶墙左侧臀部拉伸",
                "1x20''  休息 0 秒",
                "https://bqliang-1304284634.cos.ap-guangzhou.myqcloud.com/%E8%B7%91%E6%AD%A5%E6%97%A5%E8%AE%B0/%E8%B7%91%E5%90%8E%E6%8B%89%E4%BC%B8/07%E6%89%B6%E5%A2%99%E5%B7%A6%E4%BE%A7%E8%87%80%E9%83%A8%E6%8B%89%E4%BC%B8.mp4",
                """
                    要点 
                    ·单腿站立，膝盖微屈，将脚踝放于膝上，手扶墙保持平衡
                    ·挺胸直背向下坐，重心尽可能下沉
                    
                    呼吸
                    ·保持自然的呼吸
                    
                    动作感觉
                    ·左侧臀部有拉伸感
                    
                    常见错误
                    ·身体过于前倾
                """.trimIndent()
            ),
            FitnessAction(
                "扶墙右侧臀部拉伸",
                "1x20''  休息 0 秒",
                "https://bqliang-1304284634.cos.ap-guangzhou.myqcloud.com/%E8%B7%91%E6%AD%A5%E6%97%A5%E8%AE%B0/%E8%B7%91%E5%90%8E%E6%8B%89%E4%BC%B8/08%E6%89%B6%E5%A2%99%E5%8F%B3%E4%BE%A7%E8%87%80%E9%83%A8%E6%8B%89%E4%BC%B8.mp4",
                """
                    要点 
                    ·单腿站立，膝盖微屈，将脚踝放于膝上，手扶墙保持平衡
                    ·挺胸直背向下坐，重心尽可能下沉
                    
                    呼吸
                    ·保持自然的呼吸
                    
                    动作感觉
                    ·右侧臀部有拉伸感
                    
                    常见错误
                    ·身体过于前倾
                """.trimIndent()
            ),
            FitnessAction(
                "左侧小腿拉伸",
                "1x25''  休息 0 秒",
                "https://bqliang-1304284634.cos.ap-guangzhou.myqcloud.com/%E8%B7%91%E6%AD%A5%E6%97%A5%E8%AE%B0/%E8%B7%91%E5%90%8E%E6%8B%89%E4%BC%B8/09%E5%B7%A6%E4%BE%A7%E5%B0%8F%E8%85%BF%E6%8B%89%E4%BC%B8.mp4",
                """
                    要点 
                    ·脚尖抵在墙面上
                    ·身体重心往前移
                    
                    呼吸
                    ·保持均匀呼吸
                    
                    动作感觉
                    ·左侧小腿有拉伸感
                    
                    常见错误
                    ·臀部没有往前移
                """.trimIndent()
            ),
            FitnessAction(
                "右侧小腿拉伸",
                "1x25''  休息 0 秒",
                "https://bqliang-1304284634.cos.ap-guangzhou.myqcloud.com/%E8%B7%91%E6%AD%A5%E6%97%A5%E8%AE%B0/%E8%B7%91%E5%90%8E%E6%8B%89%E4%BC%B8/10%E5%8F%B3%E4%BE%A7%E5%B0%8F%E8%85%BF%E6%8B%89%E4%BC%B8.mp4",
                """
                    要点 
                    ·脚尖抵在墙面上
                    ·身体重心往前移
                    
                    呼吸
                    ·保持均匀呼吸
                    
                    动作感觉
                    ·右侧小腿有拉伸感
                    
                    常见错误
                    ·臀部没有往前移
                """.trimIndent()
            ),
        )
    )


}