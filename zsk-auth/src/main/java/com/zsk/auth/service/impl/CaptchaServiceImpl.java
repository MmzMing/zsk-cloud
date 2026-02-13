package com.zsk.auth.service.impl;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.img.ImgUtil;
import cn.hutool.core.util.RandomUtil;
import com.zsk.auth.domain.CaptchaResponse;
import com.zsk.auth.service.ICaptchaService;
import com.zsk.common.core.constant.CacheConstants;
import com.zsk.common.core.exception.AuthException;
import com.zsk.common.core.utils.StringUtils;
import com.zsk.common.redis.service.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 验证码服务实现（自定义滑块拼图验证码）
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2024-01-15
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CaptchaServiceImpl implements ICaptchaService {

    private final RedisService redisService;

    // 拼图缺口宽高
    private static final int CUT_WIDTH = 50;
    private static final int CUT_HEIGHT = 50;
    // 拼图突起/凹陷半径
    private static final int CIRCLE_R = 5;
    // 图片宽高
    private static final int IMG_WIDTH = 300;
    private static final int IMG_HEIGHT = 150;

    /**
     * 生成滑块拼图验证码
     *
     * @return 验证码响应对象（包含背景图、拼图、UUID）
     */
    @Override
    public CaptchaResponse generateSlideCaptcha() {
        try {
            // 1. 生成随机背景图
            BufferedImage bgImage = new BufferedImage(IMG_WIDTH, IMG_HEIGHT, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = bgImage.createGraphics();
            // 填充随机背景色
            g.setColor(new Color(240 + RandomUtil.randomInt(15), 240 + RandomUtil.randomInt(15), 240 + RandomUtil.randomInt(15)));
            g.fillRect(0, 0, IMG_WIDTH, IMG_HEIGHT);

            // 画一些干扰线或图形
            for (int i = 0; i < 20; i++) {
                g.setColor(new Color(RandomUtil.randomInt(255), RandomUtil.randomInt(255), RandomUtil.randomInt(255)));
                g.drawLine(RandomUtil.randomInt(IMG_WIDTH), RandomUtil.randomInt(IMG_HEIGHT),
                        RandomUtil.randomInt(IMG_WIDTH), RandomUtil.randomInt(IMG_HEIGHT));
                g.drawOval(RandomUtil.randomInt(IMG_WIDTH), RandomUtil.randomInt(IMG_HEIGHT),
                        RandomUtil.randomInt(30), RandomUtil.randomInt(30));
            }

            // 添加文字干扰
            g.setFont(new Font("Arial", Font.BOLD, 25));
            for (int i = 0; i < 5; i++) {
                g.setColor(new Color(RandomUtil.randomInt(150), RandomUtil.randomInt(150), RandomUtil.randomInt(150)));
                g.drawString(RandomUtil.randomString(1), RandomUtil.randomInt(IMG_WIDTH - 20), RandomUtil.randomInt(IMG_HEIGHT - 20) + 20);
            }

            // 2. 随机生成拼图缺口位置
            // X轴范围：CUT_WIDTH ~ IMG_WIDTH - CUT_WIDTH
            int x = RandomUtil.randomInt(IMG_WIDTH - CUT_WIDTH - 20) + CUT_WIDTH + 10;
            // Y轴范围：0 ~ IMG_HEIGHT - CUT_HEIGHT
            int y = RandomUtil.randomInt(IMG_HEIGHT - CUT_HEIGHT) + 5;

            // 3. 生成拼图块图片（透明背景）
            BufferedImage puzzleImage = new BufferedImage(CUT_WIDTH, CUT_HEIGHT, BufferedImage.TYPE_INT_ARGB);
            Graphics2D pg = puzzleImage.createGraphics();

            // 从背景图中截取拼图区域
            BufferedImage cutImage = bgImage.getSubimage(x, y, CUT_WIDTH, CUT_HEIGHT);
            pg.drawImage(cutImage, 0, 0, null);

            // 给拼图块添加边框（可选）
            pg.setColor(Color.WHITE);
            pg.setStroke(new BasicStroke(2));
            pg.drawRect(0, 0, CUT_WIDTH - 1, CUT_HEIGHT - 1);
            pg.dispose();

            // 4. 在背景图上绘制缺口（半透明遮罩）
            g.setColor(new Color(0, 0, 0, 100)); // 黑色半透明
            g.fillRect(x, y, CUT_WIDTH, CUT_HEIGHT);
            g.dispose();

            // 5. 转换图片为Base64
            String bgBase64 = "data:image/png;base64," + Base64.encode(imageToBytes(bgImage));
            String puzzleBase64 = "data:image/png;base64," + Base64.encode(imageToBytes(puzzleImage));

            // 6. 缓存X坐标用于校验
            String uuid = UUID.randomUUID().toString().replace("-", "");
            String captchaKey = CacheConstants.CAPTCHA_CODE_KEY + uuid;
            // 允许误差范围在校验时处理，这里存储准确值
            redisService.setCacheObject(captchaKey, String.valueOf(x), 5, TimeUnit.MINUTES);

            return CaptchaResponse.builder()
                    .uuid(uuid)
                    .bgUrl(bgBase64)
                    .puzzleUrl(puzzleBase64)
                    .build();

        } catch (Exception e) {
            log.error("生成滑块验证码失败", e);
            throw new AuthException("生成验证码失败");
        }
    }

    /**
     * 图片转字节数组
     */
    private byte[] imageToBytes(BufferedImage image) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            ImgUtil.write(image, "png", out);
        } catch (Exception e) {
            log.error("图片转换失败", e);
        }
        return out.toByteArray();
    }

    /**
     * 校验验证码
     *
     * @param uuid 验证码唯一标识
     * @param code 用户输入的X坐标移动距离
     * @throws AuthException 验证码无效或错误时抛出
     */
    @Override
    public void validateCaptcha(String uuid, String code) {
        if (StringUtils.isEmpty(uuid) || StringUtils.isEmpty(code)) {
            throw new AuthException("验证码不能为空");
        }

        String captchaKey = CacheConstants.CAPTCHA_CODE_KEY + uuid;
        String cachedX = redisService.getCacheObject(captchaKey);

        if (StringUtils.isEmpty(cachedX)) {
            throw new AuthException("验证码已过期");
        }

        try {
            int x = Integer.parseInt(code);
            int targetX = Integer.parseInt(cachedX);
            // 允许误差范围 ±5 像素
            if (Math.abs(x - targetX) > 5) {
                throw new AuthException("验证码错误");
            }
        } catch (NumberFormatException e) {
            throw new AuthException("验证码格式错误");
        }

        redisService.deleteObject(captchaKey);
    }
}
