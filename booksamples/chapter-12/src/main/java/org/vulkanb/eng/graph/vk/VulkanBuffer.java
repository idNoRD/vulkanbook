package org.vulkanb.eng.graph.vk;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.vma.VmaAllocationCreateInfo;
import org.lwjgl.vulkan.VkBufferCreateInfo;

import java.nio.LongBuffer;

import static org.lwjgl.system.MemoryUtil.NULL;
import static org.lwjgl.util.vma.Vma.*;
import static org.lwjgl.vulkan.VK11.*;
import static org.vulkanb.eng.graph.vk.VulkanUtils.vkCheck;

public class VulkanBuffer {

    private long allocation;
    private long buffer;
    private long mappedMemory;
    private MemoryAllocator memoryAllocator;
    private PointerBuffer pb;
    private long requestedSize;

    public VulkanBuffer(MemoryAllocator memoryAllocator, long size, int bufferUsage, int memoryUsage,
                        int requiredFlags) {
        this.memoryAllocator = memoryAllocator;
        requestedSize = size;
        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkBufferCreateInfo bufferCreateInfo = VkBufferCreateInfo.callocStack(stack)
                    .sType(VK_STRUCTURE_TYPE_BUFFER_CREATE_INFO)
                    .size(size)
                    .usage(bufferUsage)
                    .sharingMode(VK_SHARING_MODE_EXCLUSIVE);

            VmaAllocationCreateInfo allocInfo = VmaAllocationCreateInfo.callocStack(stack)
                    .requiredFlags(requiredFlags)
                    .usage(memoryUsage);

            PointerBuffer pAllocation = stack.callocPointer(1);
            LongBuffer lp = stack.mallocLong(1);
            vkCheck(vmaCreateBuffer(memoryAllocator.getVmaAllocator(), bufferCreateInfo, allocInfo, lp,
                    pAllocation, null), "Failed to create buffer");
            buffer = lp.get(0);
            allocation = pAllocation.get(0);
            pb = PointerBuffer.allocateDirect(1);
        }
    }

    public void cleanup() {
        pb.free();
        unMap();
        vmaDestroyBuffer(memoryAllocator.getVmaAllocator(), buffer, allocation);
    }

    public long getBuffer() {
        return buffer;
    }

    public long getRequestedSize() {
        return requestedSize;
    }

    public long map() {
        if (mappedMemory == NULL) {
            vkCheck(vmaMapMemory(memoryAllocator.getVmaAllocator(), allocation, pb),
                    "Failed to map allocation");
            mappedMemory = pb.get(0);
        }
        return mappedMemory;
    }

    public void unMap() {
        if (mappedMemory != NULL) {
            vmaUnmapMemory(memoryAllocator.getVmaAllocator(), allocation);
            mappedMemory = NULL;
        }
    }
}