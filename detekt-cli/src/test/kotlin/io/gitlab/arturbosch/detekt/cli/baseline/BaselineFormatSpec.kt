package io.gitlab.arturbosch.detekt.cli.baseline

import io.github.detekt.test.utils.createTempFileForTest
import io.github.detekt.test.utils.resourceAsPath
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatIllegalStateException
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import java.nio.file.Files

class BaselineFormatSpec : Spek({

    describe("baseline format") {

        context("read a baseline file") {

            it("loads the baseline file") {
                val path = resourceAsPath("/smell-baseline.xml")
                val (blacklist, whitelist) = BaselineFormat().read(path)

                assertThat(blacklist).hasSize(2)
                assertThat(blacklist).anySatisfy { it.startsWith("LongParameterList") }
                assertThat(blacklist).anySatisfy { it.startsWith("LongMethod") }
                assertThat(whitelist).hasSize(1)
                assertThat(whitelist).anySatisfy { it.startsWith("FeatureEnvy") }
            }

            it("throws on an invalid baseline file extension") {
                val path = resourceAsPath("/invalid-smell-baseline.txt")
                assertThatThrownBy { BaselineFormat().read(path) }.isInstanceOf(InvalidBaselineState::class.java)
            }

            it("throws on an invalid baseline ID declaration") {
                val path = resourceAsPath("/invalid-smell-baseline.xml")
                assertThatIllegalStateException()
                    .isThrownBy { BaselineFormat().read(path) }
                    .withMessage("The content of the ID element must not be empty")
            }
        }

        context("writes a baseline file") {

            val savedBaseline = Baseline(setOf("4", "2", "2"), setOf("1", "2", "3"))

            it("has a new line at the end of the written baseline file") {
                val tempFile = createTempFileForTest("baseline1", ".xml")

                val format = BaselineFormat()
                format.write(savedBaseline, tempFile)
                val bytes = Files.readAllBytes(tempFile)
                val content = String(bytes, Charsets.UTF_8)

                assertThat(content).endsWith(">\n")
            }

            it("asserts that the saved and loaded baseline files are equal") {
                val tempFile = createTempFileForTest("baseline-saved", ".xml")

                val format = BaselineFormat()
                format.write(savedBaseline, tempFile)
                val loadedBaseline = format.read(tempFile)

                assertThat(loadedBaseline).isEqualTo(savedBaseline)
            }
        }
    }
})
