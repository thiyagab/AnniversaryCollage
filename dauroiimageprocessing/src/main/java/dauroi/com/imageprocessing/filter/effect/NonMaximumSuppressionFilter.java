package dauroi.com.imageprocessing.filter.effect;

import dauroi.com.imageprocessing.filter.colour.TextureSamplingFilter;

public class NonMaximumSuppressionFilter extends TextureSamplingFilter {
    public static final String NMS_FRAGMENT_SHADER = "" +
            "uniform sampler2D inputImageTexture;\n" +
            "\n" +
            "varying highp vec2 textureCoordinate;\n" +
            "varying highp vec2 leftTextureCoordinate;\n" +
            "varying highp vec2 rightTextureCoordinate;\n" +
            "\n" +
            "varying highp vec2 topTextureCoordinate;\n" +
            "varying highp vec2 topLeftTextureCoordinate;\n" +
            "varying highp vec2 topRightTextureCoordinate;\n" +
            "\n" +
            "varying highp vec2 bottomTextureCoordinate;\n" +
            "varying highp vec2 bottomLeftTextureCoordinate;\n" +
            "varying highp vec2 bottomRightTextureCoordinate;\n" +
            "\n" +
            "void main()\n" +
            "{\n" +
            "lowp float bottomColor = texture2D(inputImageTexture, bottomTextureCoordinate).r;\n" +
            "lowp float bottomLeftColor = texture2D(inputImageTexture, bottomLeftTextureCoordinate).r;\n" +
            "lowp float bottomRightColor = texture2D(inputImageTexture, bottomRightTextureCoordinate).r;\n" +
            "lowp vec4 centerColor = texture2D(inputImageTexture, textureCoordinate);\n" +
            "lowp float leftColor = texture2D(inputImageTexture, leftTextureCoordinate).r;\n" +
            "lowp float rightColor = texture2D(inputImageTexture, rightTextureCoordinate).r;\n" +
            "lowp float topColor = texture2D(inputImageTexture, topTextureCoordinate).r;\n" +
            "lowp float topRightColor = texture2D(inputImageTexture, topRightTextureCoordinate).r;\n" +
            "lowp float topLeftColor = texture2D(inputImageTexture, topLeftTextureCoordinate).r;\n" +
            "\n" +
            "// Use a tiebreaker for pixels to the left and immediately above this one\n" +
            "lowp float multiplier = 1.0 - step(centerColor.r, topColor);\n" +
            "multiplier = multiplier * 1.0 - step(centerColor.r, topLeftColor);\n" +
            "multiplier = multiplier * 1.0 - step(centerColor.r, leftColor);\n" +
            "multiplier = multiplier * 1.0 - step(centerColor.r, bottomLeftColor);\n" +
            "\n" +
            "lowp float maxValue = max(centerColor.r, bottomColor);\n" +
            "maxValue = max(maxValue, bottomRightColor);\n" +
            "maxValue = max(maxValue, rightColor);\n" +
            "maxValue = max(maxValue, topRightColor);\n" +
            "\n" +
            "gl_FragColor = vec4((centerColor.rgb * step(maxValue, centerColor.r) * multiplier), 1.0);\n" +
            "}\n";

    public NonMaximumSuppressionFilter() {
        super(NMS_FRAGMENT_SHADER);
    }
}
