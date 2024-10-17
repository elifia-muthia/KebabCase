package dev.coms4156.project.kebabcase.controller;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import dev.coms4156.project.kebabcase.entity.BuildingEntity;
import dev.coms4156.project.kebabcase.entity.HousingUnitEntity;
import dev.coms4156.project.kebabcase.repository.BuildingRepositoryInterface;
import dev.coms4156.project.kebabcase.repository.HousingUnitRepositoryInterface;

/** TODO */
@RestController
public class HousingUnitController {
  private final HousingUnitRepositoryInterface housingUnitRepository;

  private final BuildingRepositoryInterface buildingRepository;

  private final ObjectMapper objectMapper;

  private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;


  public HousingUnitController(
      HousingUnitRepositoryInterface housingUnitRepository,
      BuildingRepositoryInterface buildingRepository,
      ObjectMapper objectMapper
  ) {
    this.housingUnitRepository = housingUnitRepository;
    this.buildingRepository = buildingRepository;
    this.objectMapper = objectMapper;
  }

  @GetMapping("/building/{id}/housing-units")
  public List<ObjectNode> getBuildingHousingUnits(@PathVariable int id) {

    Optional<BuildingEntity> buildingRepositoryResult = this.buildingRepository.findById(id);

    if (buildingRepositoryResult.isEmpty()) {
      throw new ResponseStatusException(
          HttpStatus.NOT_FOUND, "Building with id " + id + " not found"
      );
    }

    BuildingEntity building = buildingRepositoryResult.get();

    List<HousingUnitEntity>  housingUnits =
        this.housingUnitRepository.findByBuilding(building);

    return housingUnits.stream().map(housingUnit -> {
      ObjectNode Json = this.objectMapper.createObjectNode();
      Json.put("id", housingUnit.getId());
      Json.put("unit_number", housingUnit.getUnitNumber());
      return Json;
    }).collect(Collectors.toList());
  }

  @GetMapping("/housing-unit/{id}")
  public ObjectNode getHousingUnit(@PathVariable int id) {

    Optional<HousingUnitEntity> housingUnitRepoResult = this.housingUnitRepository.findById(id);

    if (housingUnitRepoResult.isEmpty()) {
      throw new ResponseStatusException(
          HttpStatus.NOT_FOUND, "Housing unit with id " + id + " not found"
      );
    }

    HousingUnitEntity unit = housingUnitRepoResult.get();

    ObjectNode json = this.objectMapper.createObjectNode();
    json.put("id", unit.getId());
    json.put("building_id", unit.getBuilding().getId());
    json.put("unit_number", unit.getUnitNumber());
    json.put("created_datetime", unit.getCreatedDatetime().format(formatter));
    json.put("modified_datetime", unit.getModifiedDatetime().format(formatter));

    return json;
  }
}
