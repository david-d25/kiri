package space.davids_digital.kiri.integration.openai

import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import space.davids_digital.kiri.integration.openai.OpenaiImageService.Background
import space.davids_digital.kiri.integration.openai.OpenaiImageService.OutputFormat

class OpenaiImageServiceTest {

    @Nested
    inner class SizeValidation {
        @Test
        fun `auto is accepted`() {
            assertDoesNotThrow { OpenaiImageService.validateSize(OpenaiImageService.SIZE_AUTO) }
        }

        @Test
        fun `explicit resolution within limits is accepted`() {
            assertDoesNotThrow { OpenaiImageService.validateSize("1536x1024") }
            assertDoesNotThrow { OpenaiImageService.validateSize("3840x2160") }
        }

        @Test
        fun `malformed resolution is rejected`() {
            assertThrows(IllegalArgumentException::class.java) { OpenaiImageService.validateSize("1024") }
            assertThrows(IllegalArgumentException::class.java) { OpenaiImageService.validateSize("1024*1024") }
            assertThrows(IllegalArgumentException::class.java) { OpenaiImageService.validateSize("1024x1024 ") }
        }

        @Test
        fun `sides that are not multiples of 16 are rejected`() {
            assertThrows(IllegalArgumentException::class.java) { OpenaiImageService.validateSize("1000x1000") }
        }

        @Test
        fun `side above 3840 is rejected`() {
            assertThrows(IllegalArgumentException::class.java) { OpenaiImageService.validateSize("3856x2160") }
        }

        @Test
        fun `too few pixels is rejected`() {
            assertThrows(IllegalArgumentException::class.java) { OpenaiImageService.validateSize("512x512") }
        }

        @Test
        fun `too many pixels is rejected`() {
            assertThrows(IllegalArgumentException::class.java) { OpenaiImageService.validateSize("3840x2176") }
        }

        @Test
        fun `aspect ratio beyond 3 to 1 is rejected`() {
            assertThrows(IllegalArgumentException::class.java) { OpenaiImageService.validateSize("3840x1024") }
        }
    }

    @Nested
    inner class AlphaValidation {
        @Test
        fun `transparent background is accepted for formats with an alpha channel`() {
            assertDoesNotThrow { OpenaiImageService.validateAlpha(Background.TRANSPARENT, OutputFormat.PNG) }
            assertDoesNotThrow { OpenaiImageService.validateAlpha(Background.TRANSPARENT, OutputFormat.WEBP) }
        }

        @Test
        fun `transparent background is rejected for jpeg`() {
            assertThrows(IllegalArgumentException::class.java) {
                OpenaiImageService.validateAlpha(Background.TRANSPARENT, OutputFormat.JPEG)
            }
        }

        @Test
        fun `jpeg is accepted for opaque backgrounds`() {
            assertDoesNotThrow { OpenaiImageService.validateAlpha(Background.AUTO, OutputFormat.JPEG) }
            assertDoesNotThrow { OpenaiImageService.validateAlpha(Background.OPAQUE, OutputFormat.JPEG) }
        }
    }
}
