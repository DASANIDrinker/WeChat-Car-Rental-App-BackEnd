package com.liuyuesi.demo.controller;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.function.ServerResponse;

import com.liuyuesi.demo.entity.VehicleTypeImage;
import com.liuyuesi.demo.service.DriverServiceImpl;
import com.liuyuesi.demo.service.VehicleTypeImageServiceImpl;
import com.liuyuesi.demo.service.VehicleTypeServiceImpl;

@RestController
@RequestMapping("/image")
@CrossOrigin(origins = "*", maxAge = 3600)
public class ImageController {

	@Autowired
	private VehicleTypeImageServiceImpl imageService;

	@Autowired
	private DriverServiceImpl driverService;
	
	@Autowired
	private VehicleTypeServiceImpl vehicleTypeService;
	
	// 身份信息文件保存在磁盘的地址
	@Value("${file.save.path.ids}")
	String fileSavePathId;

	@Value("${file.save.path.vehicles}")
	String fileSavePathVehicle;
	
	//上传驾驶员身份信息图片
	@RequestMapping("uploadIds")
	public String uploadImg(
			HttpServletRequest request, 
			@RequestParam("file") MultipartFile myfile,
			@RequestParam("id") String id,
			@RequestParam("label") Integer label) throws IOException {
		

		//可以通过RequestParam获得formData里的数据 
		//也可以用request.getParameter函数来获得
		
//		String tempId = request.getParameter("id");
//		System.out.println("A:"+id);
//		System.out.println("B:"+tempId);
//		System.out.println(myfile);
		
		File fir = new File(fileSavePathId);
		// 不存在则创建文件夹
		if (!fir.exists()) {
			fir.mkdirs();
		}
		// 文件的后缀名
		String suffix = myfile.getOriginalFilename().substring(myfile.getOriginalFilename().lastIndexOf("."));
		// 新文件名字 为了防止重复加上UUID
		String newFileName = UUID.randomUUID().toString().replaceAll("-", "") + suffix;
		System.out.println("filesavepath: " + fileSavePathId + "newFileName: " + newFileName);
//	     System.out.println(fileSavePath);
		// 新的文件路径
		File newFile = new File(fileSavePathId + newFileName);
		// 把文件写入新的File文件
		myfile.transferTo(newFile);
		// url路径=http + :// + server名字 + port端口 + /imges/ + newFileName
		String url = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + "/images/"
				+ newFileName;
		System.out.println(url);
		
		//如果是身份证正面照
		if(label == 1) {
			driverService.updateFrontIdUrl(id,newFileName);
		}
		//如果是身份证反面照
		else if(label == 2) {
			driverService.updateBackIdUrl(id,newFileName);
		}
		//如果是驾照照片
		else if(label == 3) {
			driverService.updateLicenseUrl(id,newFileName);
		}
		
		return url;
	};
	
	//上传车辆类型信息图片
	@RequestMapping("uploadVehicles")
	public String uploadImgVehicle(
			HttpServletRequest request, 
			@RequestParam("file") MultipartFile myfile,
			@RequestParam("vehicleTypeId") byte vehicleTypeId) throws IOException {
		

		System.out.println(vehicleTypeId);
		//可以通过RequestParam获得formData里的数据 
		//也可以用request.getParameter函数来获得
		
		File fir = new File(fileSavePathVehicle);
		// 不存在则创建文件夹
		if (!fir.exists()) {
			fir.mkdirs();
		}
		
		// 文件的后缀名
		String suffix = myfile.getOriginalFilename().substring(myfile.getOriginalFilename().lastIndexOf("."));
		// 新文件名字 为了防止重复加上UUID
		String fileName = myfile.getOriginalFilename();
		System.out.println("filesavepath: " + fileSavePathVehicle + "newFileName: " + fileName);
		
		// 新的文件路径
		File newFile = new File(fileSavePathVehicle + fileName);
		
		// 把文件写入新的File文件
		myfile.transferTo(newFile);
		
		// url路径=http + :// + server名字 + port端口 + /imges/ + newFileName
		String url = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + "/images/"
				+ fileName;
		System.out.println(url);
		
		vehicleTypeService.updateTypeUrl(vehicleTypeId,fileName);
		
		return url;
	}

	@GetMapping("/downloadVehicleIcon")
	public List<String> downloadVehicleIcon() {
		List<VehicleTypeImage> vehicleImageList = imageService.findAll();
		List<String> result = new ArrayList<String>();
		for (VehicleTypeImage image : vehicleImageList) {
			String imageSource = image.getImageUrl().concat(image.getImageName());
			result.add(imageSource);
		}
		return result;
	}
	
	//从文件中删除汽车类型图片，并删除相关数据库信息
    @GetMapping("/deleteVehicleTypeImage")
    public String delFile(@RequestParam("vehicleTypeId") byte vehicleTypeId,@RequestParam("imageName") String imageName) {
    	
    	System.out.println(vehicleTypeId);
    	System.out.println(imageName);
    	
    	//通过imageName来获取图片的名字
        String img_path = fileSavePathVehicle + imageName;

        File file = new File(img_path);
        //数据库中删除数据
        int i = vehicleTypeService.deleteVehicleTypeImage(vehicleTypeId);
        if(i>0){
            if (file.exists()) {//文件是否存在
                if (file.delete()) {//存在就删了
                    return "删除成功";
                } else {
                    return "文件删除失败";
                }
            }else {
                return "文件不存在";
            }
        }else {
            return "数据删除失败";
        }
    }
    
    
    //从文件中删除身份证正面图片，并删除相关数据库信息
    @GetMapping("/deleteIdFrontImage")
    public String delIdFront(
    		@RequestParam("id") String id,
    		@RequestParam("idFrontUrl") String idFrontUrl) {
    	
    	System.out.println(id);
    	System.out.println(idFrontUrl);
    	
    	//通过imageName来获取图片的名字
        String img_path = fileSavePathId + idFrontUrl;

        File file = new File(img_path);
        //数据库中删除数据
        int i = driverService.deleteFrontIdUrl(id);
        if(i>0){
            if (file.exists()) {//文件是否存在
                if (file.delete()) {//存在就删了
                    return "删除成功";
                } else {
                    return "文件删除失败";
                }
            }else {
                return "文件不存在";
            }
        }else {
            return "数据删除失败";
        }
    }
    
    //从文件中删除身份证背面图片，并删除相关数据库信息
    @GetMapping("/deleteIdBackImage")
    public String delIdBack(
    		@RequestParam("id") String id,
    		@RequestParam("idBackUrl") String idBackUrl) {
    	
    	System.out.println(id);
    	System.out.println(idBackUrl);
    	
    	//通过imageName来获取图片的名字
        String img_path = fileSavePathId + idBackUrl;

        File file = new File(img_path);
        //数据库中删除数据
        int i = driverService.deleteBackIdUrl(id);
        if(i>0){
            if (file.exists()) {//文件是否存在
                if (file.delete()) {//存在就删了
                    return "删除成功";
                } else {
                    return "文件删除失败";
                }
            }else {
                return "文件不存在";
            }
        }else {
            return "数据删除失败";
        }
    }
    
  //从文件中删除驾驶证图片，并删除相关数据库信息
    @GetMapping("/deleteLicenseImage")
    public String delLicense(
    		@RequestParam("id") String id,
    		@RequestParam("licenseUrl") String licenseUrl) {
    	
    	System.out.println(id);
    	System.out.println(licenseUrl);
    	
    	//通过imageName来获取图片的名字
        String img_path = fileSavePathId + licenseUrl;

        File file = new File(img_path);
        //数据库中删除数据
        int i = driverService.deleteLicenseUrl(id);
        if(i>0){
            if (file.exists()) {//文件是否存在
                if (file.delete()) {//存在就删了
                    return "删除成功";
                } else {
                    return "文件删除失败";
                }
            }else {
                return "文件不存在";
            }
        }else {
            return "数据删除失败";
        }
    }

}
