package com.donut.mixmessage.util.encode.encoders

import com.donut.mixmessage.util.encode.encoders.bean.AlphabetCoder

object BuddhaEncoder : AlphabetCoder(
    """
        南无萨怛他苏伽多耶阿啰诃帝三藐菩陀写佛俱胝瑟尼钐婆勃地跢鞞弊知喃娑舍迦僧卢鸡罗汉哆波那羯唎弥底提离赧悉毗奴揭摩跋因
        嚧乌般酰夜野拏槃遮慕剌目尸泥头阇茶输西刍沙吠柱补师毖怜捺母曳都瓢翳昙嚂视耽扬歧部叱你密儜盘叉突咤乏伐赭失若崩冰刹呼蓝难
        吉具战持迭税誓礼腾罔制喝质擅扇商乾啒菟折顿稚奢夷忏掘梵印兔么
        囘虎雍瞻药者点树室口隶罂曼薄主祇斫剑坛条私毕鸠荼单度播檀车社忙谜女比嗔讫担演埵达咄耆羊索四粹普钵什频
        泮牟素闼丹狼枳涩犁利继缚丁乂丈末婢迟蔑唠文逻五略布史颇闭宅袪革姥堙坠讬鼻绮钳佉惮迄栗邬常房盎建路凌喻敛肆引赖辫殊毘侄唵谤
        瓮莎
    """.trimIndent().replace("\n", "").toList()
) {

    override val name: String = "与佛论禅"

}