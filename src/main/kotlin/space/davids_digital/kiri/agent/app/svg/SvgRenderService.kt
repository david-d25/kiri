package space.davids_digital.kiri.agent.app.svg

import org.apache.batik.transcoder.TranscoderInput
import org.apache.batik.transcoder.TranscoderOutput
import org.apache.batik.transcoder.image.PNGTranscoder
import org.springframework.stereotype.Service
import java.io.ByteArrayOutputStream
import java.io.StringReader

@Service
class SvgRenderService {
    fun renderToPng(svgContent: String, width: Int = 800, height: Int = 600): ByteArray {
        val transcoder = PNGTranscoder().apply {
            addTranscodingHint(PNGTranscoder.KEY_WIDTH, width.toFloat())
            addTranscodingHint(PNGTranscoder.KEY_HEIGHT, height.toFloat())
        }
        val output = ByteArrayOutputStream()
        transcoder.transcode(
            TranscoderInput(StringReader(svgContent)),
            TranscoderOutput(output)
        )
        return output.toByteArray()
    }
}
